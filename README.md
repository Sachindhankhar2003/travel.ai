# Travel.ai

AI-powered travel companion for Delhi NCR, Haryana & Rajasthan — built with Kotlin + Jetpack Compose + FastAPI.

---

## APIs Used

| API | Endpoint | Key Required | Where to Get |
|-----|----------|-------------|--------------|
| **Open-Meteo** | `https://api.open-meteo.com/` | ❌ No | Free, no sign-up |
| **Photon Geocoding** | `https://photon.komoot.io/` | ❌ No | Free, no sign-up |
| **OSRM Routing** | `https://router.project-osrm.org/` | ❌ No | Free, no sign-up |
| **OpenTripMap** | `https://api.opentripmap.com/0.1/` | ✅ Yes | [opentripmap.io/product](https://opentripmap.io/product) — free tier |
| **Wikipedia REST** | `https://en.wikipedia.org/api/rest_v1/` | ❌ No | Free, no sign-up |
| **Overpass (OSM)** | `https://overpass-api.de/api/interpreter` | ❌ No | Free, no sign-up |
| **Delhi OTD GTFS** | `https://otd.delhi.gov.in/data/static/` | ❌ No | Free download |
| **Travel.ai Backend** | `http://localhost:8000/` | ✅ Admin key | Self-hosted, see below |

---

## Backend Setup (FastAPI + MongoDB)

### Prerequisites
- Python 3.11+
- MongoDB running locally (port 27017) or Atlas URI

### Steps

```bash
cd backend
python -m venv venv
# Windows:
venv\Scripts\activate
# macOS/Linux:
source venv/bin/activate

pip install -r requirements.txt

# Copy env file and fill values
cp .env.example .env   # set MONGO_URL and ADMIN_API_KEY

# Seed sample data
python seed.py

# Run dev server
uvicorn main:app --reload --port 8000
```

### Backend Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Health check |
| GET | `/buses/search?from=&to=&type=` | Search bus routes |
| GET | `/buses/{id}` | Get single bus |
| GET | `/metro/route?from=&to=` | Metro route between stations |
| GET | `/pricing/hotels?city=&type=` | Nightly hotel rate (BUDGET/MID/PREMIUM) |
| POST | `/admin/buses` | Create bus (needs `X-Admin-Key` header) |
| PUT | `/admin/buses/{id}` | Update bus |
| DELETE | `/admin/buses/{id}` | Delete bus |

---

## Android Setup

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 35

### Steps

```bash
# 1. Clone the repo
git clone https://github.com/your-username/Travel.ai.git
cd Travel.ai

# 2. Create local.properties (project root)
cat > local.properties << 'EOF'
BACKEND_BASE_URL=http://10.0.2.2:8000/
ADMIN_API_KEY=citygo-admin-key-2024
OPENTRIPMAP_API_KEY=YOUR_KEY_HERE
EOF

# 3. Open in Android Studio → Run on emulator/device
```

### Key Technologies
- **Architecture**: MVVM + Clean Architecture + Hilt
- **UI**: Jetpack Compose + Material 3
- **Networking**: Retrofit + OkHttp + Moshi
- **Local DB**: Room (cache + trip plans)
- **Preferences**: DataStore
- **Maps**: osmdroid (OpenStreetMap)
- **Font**: Plus Jakarta Sans (Google Fonts)

---

## Verification Checklist

See [final-checklist](#final-checklist) below.

---

## Final Checklist {#final-checklist}

1. **Weather** — Open `Home` tab → weather card loads with temperature, humidity, wind
2. **Geocoding** — City picker search returns results from Photon
3. **Route (OSRM)** — `Map` tab → tap two points → route polyline drawn
4. **Metro GTFS** — `Transit` tab → type `Rajiv Chowk` → metro route appears
5. **Bus search** — `Transit` tab → bus search returns results from MongoDB
6. **Places** — `Explore` tab → categories tap shows OpenTripMap results (needs key)
7. **Hotels pricing** — Open `Budget` tab → calculate → stay cost uses `/pricing/hotels`
8. **Budget calculation** — Fill form → tap Calculate → total card + donut chart appear
9. **Offline mode** — Disable network → reopen app → offline banner visible, cached data shown
10. **Backend health** — `curl http://localhost:8000/health` → `{"status":"ok"}`
