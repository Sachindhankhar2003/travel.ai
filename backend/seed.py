"""
CityGo Backend - Seed Script
Seeds MongoDB with sample bus and metro route data
for Delhi-NCR region routes.

Run: python seed.py
"""
import asyncio
from motor.motor_asyncio import AsyncIOMotorClient
from datetime import datetime
import os

MONGO_URL = os.getenv("MONGO_URL", "mongodb://localhost:27017")
DB_NAME = "citygo"

client = AsyncIOMotorClient(MONGO_URL)
db = client[DB_NAME]


# ─── Bus Data ─────────────────────────────────────────────────────────────────
BUS_DATA = [
    # ── Delhi ↔ Noida ──────────────────────────────────────────────────────────
    {
        "route_number": "DL-EN-1",
        "from": "Delhi",
        "to": "Noida",
        "stops": ["ISBT Kashmere Gate", "Connaught Place", "Pragati Maidan", "Mayur Vihar Phase 1", "Noida Sector 15", "Noida Sector 18", "Noida Sector 22", "Noida City Centre"],
        "operator_type": "Government",
        "operator_name": "DTC",
        "fare": 30.0,
        "frequency_minutes": 10,
        "first_bus": "05:00",
        "last_bus": "23:30",
        "ac": False,
        "created_at": datetime.utcnow().isoformat()
    },
    {
        "route_number": "NMB-01",
        "from": "Delhi",
        "to": "Noida",
        "stops": ["New Delhi Railway Station", "ITO", "Noida Sector 15", "Noida Sector 16", "Noida Sector 18", "Atta Market Noida", "Sector 62 Noida"],
        "operator_type": "Government",
        "operator_name": "UPSRTC / Noida Metro Bus",
        "fare": 25.0,
        "frequency_minutes": 15,
        "first_bus": "05:30",
        "last_bus": "22:00",
        "ac": False,
        "created_at": datetime.utcnow().isoformat()
    },
    {
        "route_number": "OLA-N1",
        "from": "Delhi",
        "to": "Noida",
        "stops": ["Connaught Place", "Akshardham", "Sector 18 Noida"],
        "operator_type": "Private",
        "operator_name": "Ola Shuttle",
        "fare": 50.0,
        "frequency_minutes": 30,
        "first_bus": "07:00",
        "last_bus": "21:00",
        "ac": True,
        "created_at": datetime.utcnow().isoformat()
    },

    # ── Delhi ↔ Gurugram ───────────────────────────────────────────────────────
    {
        "route_number": "DL-GGN-1",
        "from": "Delhi",
        "to": "Gurugram",
        "stops": ["ISBT Dhaula Kuan", "Munirka", "Vasant Vihar", "Mahipalpur", "Rajokri", "Udyog Vihar", "Iffco Chowk", "MG Road", "Gurugram City Centre"],
        "operator_type": "Government",
        "operator_name": "Haryana Roadways",
        "fare": 35.0,
        "frequency_minutes": 12,
        "first_bus": "05:00",
        "last_bus": "23:00",
        "ac": False,
        "created_at": datetime.utcnow().isoformat()
    },
    {
        "route_number": "DL-GGN-AC",
        "from": "Delhi",
        "to": "Gurugram",
        "stops": ["Dhaula Kuan", "Sikanderpur", "Cyber City Gurugram"],
        "operator_type": "Government",
        "operator_name": "DIMTS Volvo",
        "fare": 75.0,
        "frequency_minutes": 20,
        "first_bus": "06:00",
        "last_bus": "22:00",
        "ac": True,
        "created_at": datetime.utcnow().isoformat()
    },
    {
        "route_number": "RIDESHARE-G",
        "from": "Delhi",
        "to": "Gurugram",
        "stops": ["Connaught Place", "Mehrauli", "Sohna Road", "DLF Cyber City"],
        "operator_type": "Private",
        "operator_name": "QuickRide",
        "fare": 60.0,
        "frequency_minutes": 25,
        "first_bus": "07:30",
        "last_bus": "20:00",
        "ac": True,
        "created_at": datetime.utcnow().isoformat()
    },

    # ── Delhi ↔ Jaipur ─────────────────────────────────────────────────────────
    {
        "route_number": "HR-JP-VOLVO",
        "from": "Delhi",
        "to": "Jaipur",
        "stops": ["ISBT Kashmere Gate Delhi", "Rewari", "Neemrana", "Shahjahanpur", "Kotputli", "Chomu", "Jaipur Sindhi Camp"],
        "operator_type": "Government",
        "operator_name": "Rajasthan Roadways Volvo",
        "fare": 350.0,
        "frequency_minutes": 60,
        "first_bus": "05:00",
        "last_bus": "23:00",
        "ac": True,
        "created_at": datetime.utcnow().isoformat()
    },
    {
        "route_number": "HR-JP-ORD",
        "from": "Delhi",
        "to": "Jaipur",
        "stops": ["ISBT Kashmere Gate Delhi", "Gurgaon Bus Stand", "Alwar", "Shahjahanpur", "Behror", "Kotputli", "Jaipur Sindhi Camp"],
        "operator_type": "Government",
        "operator_name": "Haryana Roadways",
        "fare": 210.0,
        "frequency_minutes": 30,
        "first_bus": "04:30",
        "last_bus": "22:30",
        "ac": False,
        "created_at": datetime.utcnow().isoformat()
    },
    {
        "route_number": "RJp-PVT-01",
        "from": "Delhi",
        "to": "Jaipur",
        "stops": ["Dhaula Kuan", "Manesar", "Dharuhera", "Alwar Bypass", "Chomu", "Jaipur"],
        "operator_type": "Private",
        "operator_name": "Raj Express",
        "fare": 400.0,
        "frequency_minutes": 45,
        "first_bus": "06:00",
        "last_bus": "22:00",
        "ac": True,
        "created_at": datetime.utcnow().isoformat()
    },

    # ── Delhi ↔ Chandigarh ────────────────────────────────────────────────────
    {
        "route_number": "CDG-VOLVO",
        "from": "Delhi",
        "to": "Chandigarh",
        "stops": ["ISBT Kashmere Gate Delhi", "Murthal", "Panipat", "Karnal", "Ambala", "Chandigarh ISBT 43"],
        "operator_type": "Government",
        "operator_name": "HRTC Volvo",
        "fare": 450.0,
        "frequency_minutes": 30,
        "first_bus": "04:00",
        "last_bus": "23:00",
        "ac": True,
        "created_at": datetime.utcnow().isoformat()
    },
    {
        "route_number": "CDG-ORD",
        "from": "Delhi",
        "to": "Chandigarh",
        "stops": ["ISBT Kashmere Gate Delhi", "Kundli", "Sonipat", "Gohana", "Panipat", "Kurukshetra", "Ambala", "Chandigarh"],
        "operator_type": "Government",
        "operator_name": "Haryana Roadways",
        "fare": 250.0,
        "frequency_minutes": 20,
        "first_bus": "03:30",
        "last_bus": "22:30",
        "ac": False,
        "created_at": datetime.utcnow().isoformat()
    },

    # ── Ghaziabad ↔ Meerut ────────────────────────────────────────────────────
    {
        "route_number": "GZB-MRT-1",
        "from": "Ghaziabad",
        "to": "Meerut",
        "stops": ["Ghaziabad Bus Stand", "Modinagar", "Muradnagar", "Hapur Road", "Meerut City"],
        "operator_type": "Government",
        "operator_name": "UPSRTC",
        "fare": 60.0,
        "frequency_minutes": 15,
        "first_bus": "05:00",
        "last_bus": "22:00",
        "ac": False,
        "created_at": datetime.utcnow().isoformat()
    },
    {
        "route_number": "GZB-MRT-EXP",
        "from": "Ghaziabad",
        "to": "Meerut",
        "stops": ["Ghaziabad Railway Station", "Modinagar", "Meerut Cantt"],
        "operator_type": "Private",
        "operator_name": "Singh Travels",
        "fare": 80.0,
        "frequency_minutes": 20,
        "first_bus": "06:00",
        "last_bus": "21:00",
        "ac": False,
        "created_at": datetime.utcnow().isoformat()
    }
]


# ─── Metro Route Data ─────────────────────────────────────────────────────────
METRO_ROUTES = [
    {
        "from_station": "Rajiv Chowk",
        "to_station": "HUDA City Centre",
        "line": "Yellow Line",
        "stations": ["Rajiv Chowk", "Patel Chowk", "Central Secretariat", "Udyog Bhawan",
                    "Lok Kalyan Marg", "Jor Bagh", "INA", "AIIMS", "Green Park",
                    "Hauz Khas", "Malviya Nagar", "Saket", "Qutab Minar", "Chhattarpur",
                    "Sultanpur", "Ghitorni", "Arjan Garh", "Guru Dronacharya", "Sikanderpur",
                    "MG Road", "IFFCO Chowk", "HUDA City Centre"],
        "station_count": 22,
        "fare": 60.0,
        "estimated_time_minutes": 48,
        "first_train": "05:30",
        "last_train": "23:00",
        "interchange_stations": []
    },
    {
        "from_station": "Kashmere Gate",
        "to_station": "Rajiv Chowk",
        "line": "Yellow Line",
        "stations": ["Kashmere Gate", "Chandni Chowk", "Chawri Bazar", "New Delhi",
                    "Rajiv Chowk"],
        "station_count": 5,
        "fare": 30.0,
        "estimated_time_minutes": 12,
        "first_train": "05:30",
        "last_train": "23:00",
        "interchange_stations": []
    },
    {
        "from_station": "Dwarka Sector 21",
        "to_station": "Noida Electronic City",
        "line": "Blue Line",
        "stations": ["Dwarka Sector 21", "Dwarka Sector 8", "Dwarka Sector 9", "Dwarka Sector 10",
                    "Dwarka Sector 11", "Dwarka Sector 12", "Dwarka Sector 13", "Dwarka Sector 14",
                    "Dwarka", "Dwarka Mor", "Nawada", "Uttam Nagar West", "Uttam Nagar East",
                    "Janakpuri West", "Janakpuri East", "Tilak Nagar", "Subhash Nagar",
                    "Tagore Garden", "Rajouri Garden", "Ramesh Nagar", "Moti Nagar",
                    "Kirti Nagar", "Shadipur", "Patel Nagar", "Rajendra Place", "Karol Bagh",
                    "Jhandewalan", "Ramakrishna Ashram Marg", "Rajiv Chowk", "Mandi House",
                    "Supreme Court", "Pragati Maidan", "Indraprastha", "Yamuna Bank",
                    "Akshardham", "Mayur Vihar - 1", "Mayur Vihar Extension", "New Ashok Nagar",
                    "Noida Sector 15", "Noida Sector 16", "Noida Sector 18", "Botanical Garden",
                    "Noida Sector 52", "Noida Sector 61", "Noida Sector 59", "Noida Sector 62",
                    "Noida Electronic City"],
        "station_count": 47,
        "fare": 70.0,
        "estimated_time_minutes": 88,
        "first_train": "05:30",
        "last_train": "22:30",
        "interchange_stations": ["Rajiv Chowk", "Yamuna Bank"]
    },
    {
        "from_station": "Rajiv Chowk",
        "to_station": "Noida Sector 18",
        "line": "Blue Line",
        "stations": ["Rajiv Chowk", "Mandi House", "Supreme Court", "Pragati Maidan",
                    "Indraprastha", "Yamuna Bank", "Akshardham", "Mayur Vihar - 1",
                    "Mayur Vihar Extension", "New Ashok Nagar", "Noida Sector 15",
                    "Noida Sector 16", "Noida Sector 18"],
        "station_count": 13,
        "fare": 40.0,
        "estimated_time_minutes": 28,
        "first_train": "05:30",
        "last_train": "22:30",
        "interchange_stations": []
    },
    {
        "from_station": "Sikanderpur",
        "to_station": "Belvedere Towers",
        "line": "Rapid Metro",
        "stations": ["Sikanderpur", "Sector 42-43", "Sector 53-54", "Sector 54 Chowk",
                    "Sector 55-56", "Belvedere Towers"],
        "station_count": 6,
        "fare": 20.0,
        "estimated_time_minutes": 12,
        "first_train": "06:00",
        "last_train": "22:00",
        "interchange_stations": []
    },
    {
        "from_station": "Noida Sector 51",
        "to_station": "Pari Chowk",
        "line": "Aqua Line",
        "stations": ["Sector 51", "Sector 76", "Sector 101", "Sector 81", "NSEZ",
                    "Sector 83", "Sector 137", "Sector 142", "Sector 143", "Sector 144",
                    "Sector 145", "Sector 146", "Sector 147", "Sector 148", "Pari Chowk"],
        "station_count": 15,
        "fare": 40.0,
        "estimated_time_minutes": 25,
        "first_train": "06:00",
        "last_train": "22:00",
        "interchange_stations": []
    }
]


# ─── Seed Function ─────────────────────────────────────────────────────────────
async def seed():
    print("Connecting to MongoDB...")
    
    # Clear existing data
    await db.buses.delete_many({})
    await db.metro_routes.delete_many({})
    print("Cleared existing data")

    # Insert buses
    result = await db.buses.insert_many(BUS_DATA)
    print(f"Inserted {len(result.inserted_ids)} bus routes")

    # Insert metro routes
    result = await db.metro_routes.insert_many(METRO_ROUTES)
    print(f"Inserted {len(result.inserted_ids)} metro routes")

    # Create indices
    await db.buses.create_index([("from", "text"), ("to", "text"), ("stops", "text")])
    await db.buses.create_index("operator_type")
    await db.metro_routes.create_index([("from_station", 1), ("to_station", 1)])
    print("Created indices")

    print("\nSeed complete!")
    print(f"  Buses: {len(BUS_DATA)} routes")
    print(f"  Metro routes: {len(METRO_ROUTES)} routes")
    print("\nSample routes seeded:")
    print("  - Delhi <-> Noida (DTC, UPSRTC, Ola Shuttle)")
    print("  - Delhi <-> Gurugram (Haryana Roadways, DIMTS Volvo, Private)")
    print("  - Delhi <-> Jaipur (Rajasthan Roadways, Haryana Roadways, Private)")
    print("  - Delhi <-> Chandigarh (HRTC Volvo, Haryana Roadways)")
    print("  - Ghaziabad <-> Meerut (UPSRTC, Private)")
    print("  - Metro: Yellow Line, Blue Line, Rapid Metro, Aqua Line routes")


if __name__ == "__main__":
    asyncio.run(seed())
