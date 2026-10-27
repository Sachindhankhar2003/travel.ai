from fastapi import FastAPI, HTTPException, Header, Query, Path
from fastapi.middleware.cors import CORSMiddleware
from motor.motor_asyncio import AsyncIOMotorClient
from pydantic import BaseModel, Field
from typing import Optional, List
from bson import ObjectId
from datetime import datetime
import os

# ─── App Setup ────────────────────────────────────────────────────────────────
app = FastAPI(
    title="CityGo Transit API",
    description="Backend for CityGo - Delhi NCR Transit App",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ─── DB Setup ─────────────────────────────────────────────────────────────────
MONGO_URL = os.getenv("MONGO_URL", "mongodb://localhost:27017")
ADMIN_API_KEY = os.getenv("ADMIN_API_KEY", "citygo-admin-key-2024")
DB_NAME = "citygo"

client = AsyncIOMotorClient(MONGO_URL)
db = client[DB_NAME]

# ─── Models ───────────────────────────────────────────────────────────────────
class PyObjectId(ObjectId):
    @classmethod
    def __get_validators__(cls):
        yield cls.validate

    @classmethod
    def validate(cls, v):
        if not ObjectId.is_valid(v):
            raise ValueError("Invalid ObjectId")
        return ObjectId(v)

    @classmethod
    def __modify_schema__(cls, field_schema):
        field_schema.update(type="string")


class BusBase(BaseModel):
    route_number: str
    from_: str = Field(..., alias="from")
    to: str
    stops: List[str]
    operator_type: str       # "Government" | "Private"
    operator_name: str
    fare: float
    frequency_minutes: int
    first_bus: str
    last_bus: str
    ac: bool = False

    class Config:
        populate_by_name = True
        json_schema_extra = {
            "example": {
                "route_number": "DL-1",
                "from": "Delhi",
                "to": "Noida",
                "stops": ["Connaught Place", "Pragati Maidan", "Mayur Vihar", "Sector 18 Noida"],
                "operator_type": "Government",
                "operator_name": "DTC",
                "fare": 30.0,
                "frequency_minutes": 15,
                "first_bus": "05:30",
                "last_bus": "23:00",
                "ac": False
            }
        }


class BusCreate(BusBase):
    pass


class BusResponse(BusBase):
    id: str = Field(alias="_id")

    class Config:
        populate_by_name = True


class MetroStation(BaseModel):
    station_id: str
    name: str
    line: str
    line_color: str
    latitude: float
    longitude: float
    sequence_on_line: int
    is_interchange: bool = False
    interchange: Optional[str] = None
    first_train: str = "05:30"
    last_train: str = "23:00"


class MetroRouteResponse(BaseModel):
    from_station: str
    to_station: str
    line: str
    stations: List[str]
    station_count: int
    fare: float
    estimated_time_minutes: int
    first_train: str
    last_train: str
    interchange_stations: List[str] = []


class BusSearchResponse(BaseModel):
    buses: List[dict]
    total: int


# ─── Helpers ──────────────────────────────────────────────────────────────────
def bus_to_dict(bus: dict) -> dict:
    bus["_id"] = str(bus["_id"])
    return bus


def verify_admin(api_key: str = Header(alias="X-Admin-Key")):
    if api_key != ADMIN_API_KEY:
        raise HTTPException(status_code=403, detail="Invalid admin API key")
    return api_key


# ─── Bus Routes ───────────────────────────────────────────────────────────────
@app.get("/buses/search", response_model=BusSearchResponse)
async def search_buses(
    from_: str = Query(..., alias="from"),
    to: str = Query(...),
    type: str = Query("all", regex="^(all|govt|private)$")
):
    """Search buses between two locations"""
    query: dict = {
        "$or": [
            {"from": {"$regex": from_, "$options": "i"}},
            {"stops": {"$elemMatch": {"$regex": from_, "$options": "i"}}}
        ],
        "$and": [
            {
                "$or": [
                    {"to": {"$regex": to, "$options": "i"}},
                    {"stops": {"$elemMatch": {"$regex": to, "$options": "i"}}}
                ]
            }
        ]
    }

    if type == "govt":
        query["operator_type"] = "Government"
    elif type == "private":
        query["operator_type"] = "Private"

    cursor = db.buses.find(query)
    buses = [bus_to_dict(bus) async for bus in cursor]
    return BusSearchResponse(buses=buses, total=len(buses))


@app.get("/buses/{bus_id}")
async def get_bus(bus_id: str = Path(...)):
    """Get a specific bus by ID"""
    if not ObjectId.is_valid(bus_id):
        raise HTTPException(status_code=400, detail="Invalid bus ID")
    bus = await db.buses.find_one({"_id": ObjectId(bus_id)})
    if not bus:
        raise HTTPException(status_code=404, detail="Bus not found")
    return bus_to_dict(bus)


# ─── Metro Routes ─────────────────────────────────────────────────────────────
@app.get("/metro/route", response_model=MetroRouteResponse)
async def get_metro_route(
    from_: str = Query(..., alias="from"),
    to: str = Query(...)
):
    """Get metro route between two stations"""
    # Try from DB first
    cached = await db.metro_routes.find_one({
        "from_station": {"$regex": from_, "$options": "i"},
        "to_station": {"$regex": to, "$options": "i"}
    })
    if cached:
        cached.pop("_id", None)
        return cached

    # Try reverse
    cached_reverse = await db.metro_routes.find_one({
        "from_station": {"$regex": to, "$options": "i"},
        "to_station": {"$regex": from_, "$options": "i"}
    })
    if cached_reverse:
        # Reverse the route
        route = MetroRouteResponse(
            from_station=from_,
            to_station=to,
            line=cached_reverse["line"],
            stations=list(reversed(cached_reverse["stations"])),
            station_count=cached_reverse["station_count"],
            fare=cached_reverse["fare"],
            estimated_time_minutes=cached_reverse["estimated_time_minutes"],
            first_train=cached_reverse["first_train"],
            last_train=cached_reverse["last_train"],
            interchange_stations=cached_reverse.get("interchange_stations", [])
        )
        return route

    raise HTTPException(
        status_code=404,
        detail=f"No metro route found between '{from_}' and '{to}'"
    )


# ─── Admin Routes ─────────────────────────────────────────────────────────────
@app.post("/admin/buses", status_code=201)
async def create_bus(
    bus: BusCreate,
    api_key: str = Header(alias="X-Admin-Key")
):
    verify_admin(api_key)
    bus_dict = bus.dict(by_alias=True)
    bus_dict["created_at"] = datetime.utcnow().isoformat()
    result = await db.buses.insert_one(bus_dict)
    created = await db.buses.find_one({"_id": result.inserted_id})
    return bus_to_dict(created)


@app.put("/admin/buses/{bus_id}")
async def update_bus(
    bus: BusCreate,
    bus_id: str = Path(...),
    api_key: str = Header(alias="X-Admin-Key")
):
    verify_admin(api_key)
    if not ObjectId.is_valid(bus_id):
        raise HTTPException(status_code=400, detail="Invalid bus ID")
    
    bus_dict = bus.dict(by_alias=True)
    bus_dict["updated_at"] = datetime.utcnow().isoformat()
    result = await db.buses.find_one_and_update(
        {"_id": ObjectId(bus_id)},
        {"$set": bus_dict},
        return_document=True
    )
    if not result:
        raise HTTPException(status_code=404, detail="Bus not found")
    return bus_to_dict(result)


@app.delete("/admin/buses/{bus_id}")
async def delete_bus(
    bus_id: str = Path(...),
    api_key: str = Header(alias="X-Admin-Key")
):
    verify_admin(api_key)
    if not ObjectId.is_valid(bus_id):
        raise HTTPException(status_code=400, detail="Invalid bus ID")
    result = await db.buses.delete_one({"_id": ObjectId(bus_id)})
    if result.deleted_count == 0:
        raise HTTPException(status_code=404, detail="Bus not found")
    return {"message": "Bus deleted successfully", "id": bus_id}


# ─── Health Check ─────────────────────────────────────────────────────────────
@app.get("/health")
async def health():
    return {"status": "ok", "service": "Travel.ai API", "version": "1.0.0"}


# ─── Hotel Pricing ────────────────────────────────────────────────────────────

class HotelPricingResponse(BaseModel):
    city: str
    type: str
    price_per_night: float
    currency: str = "INR"

ROOM_PRICES: dict = {
    "BUDGET":  {"default": 800.0},
    "MID":     {"default": 2000.0},
    "PREMIUM": {"default": 5000.0},
    # City overrides
    "Delhi_BUDGET": 900.0, "Delhi_MID": 2500.0, "Delhi_PREMIUM": 6000.0,
    "Jaipur_BUDGET": 700.0, "Jaipur_MID": 1800.0, "Jaipur_PREMIUM": 4500.0,
    "Agra_BUDGET": 600.0,  "Agra_MID": 1500.0,  "Agra_PREMIUM": 4000.0,
    "Shimla_BUDGET": 1200.0, "Shimla_MID": 3000.0, "Shimla_PREMIUM": 7000.0,
}

@app.get("/pricing/hotels", response_model=HotelPricingResponse)
async def get_hotel_pricing(
    city: str = Query(..., description="Destination city name"),
    type: str = Query("MID", regex="^(BUDGET|MID|PREMIUM)$")
):
    """Return nightly hotel rate for a given city and room type"""
    key = f"{city}_{type}"
    price = ROOM_PRICES.get(key) or ROOM_PRICES.get(type, {}).get("default", 1500.0)
    # Also try from DB if seeded
    db_entry = await db.hotel_pricing.find_one({"city": {"$regex": city, "$options": "i"}, "type": type})
    if db_entry:
        price = db_entry.get("price_per_night", price)
    return HotelPricingResponse(city=city, type=type, price_per_night=price)


# ─── Dev Entry Point ──────────────────────────────────────────────────────────
if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
