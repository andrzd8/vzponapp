# VzponApp 🏔️

Android aplikacija za slovensko pohodništvo, razvita v okviru predmeta TNUV na Fakulteti za elektrotehniko, Univerza v Ljubljani.

---

## Opis

VzponApp je mobilna aplikacija za načrtovanje in ogledovanje planinskih poti v Sloveniji. Uporabniku nudi pregled tras, interaktivni GPX zemljevid, vremensko varnostno oceno in osebni profil glede na izkušenost.

---

## Funkcionalnosti

### 🧭 Onboarding
- Ob prvem zagonu aplikacija uporabnika pozdravi z uvodnimi zasloni
- Uporabnik vnese ime in izbere stopnjo izkušenosti: **Pohodnik**, **Gornik** ali **Alpinist**
- Podatki se shranijo v SharedPreferences in vplivajo na varnostno oceno poti

### 🏠 Domači zaslon (HomeActivity)
- Horizontalni swipe seznam planinskih poti z zaokroženimi karticami
- Vsaka kartica prikazuje ime poti, razdaljo, težavnost in vremensko varnostno oceno
- Varnostna ocena se izračuna glede na trenutno vreme, nadmorsko višino in izkušenost uporabnika
- Navigacija na dno zaslona z ikonami (domov, zemljevid, profil)

### 🗺️ Zemljevid (MapActivity)
- Interaktivni OSM (OpenStreetMap) zemljevid z uporabo knjižnice **osmdroid**
- Prikaz GPX sledi izbrane poti kot barvna linija na zemljevidu
- Samodejna centracija in zoom na meje trase
- Animiran ARSO radarski overlay s padavinami v realnem času
  - GIF animacija s pravimi geografskimi mejami za Slovenijo
  - HSV-filtriranje transparentnosti za čist prikaz
  - Animirana legenda z oznako časa posameznih sličic

### ☁️ Vreme & varnostna ocena
- Podatki se pridobivajo iz **OpenWeatherMap API** (endpoint `/data/2.5/weather`)
- 3-urno predpomnjenje v SharedPreferences za varčevanje s klici
- `WeatherService.getSafetyLabel()` izračuna oceno:
  - 🟢 **Varno** – ugodne razmere
  - 🟡 **Previdno** – zmerno tveganje (dež, nadmorska višina, dolga tura)
  - 🔴 **Nevarno** – nevihte ali ekstremni pogoji
- Upošteva: vrsto vremena, nadmorsko višino (ključna meja 1000 m), izkušenost in dolžino poti

### 📋 Podrobnosti poti (TrailDetailsActivity)
- Prikaz naziva, razdalje in varnostne ocene
- Možnost shranjevanja poti v profil

### 👤 Profil (ProfileActivity)
- Prikaz imena in stopnje izkušenosti
- Seznam shranjenih tur

### ➕ Dodajanje poti (AddTrailActivity)
- Uvoz GPX datoteke iz naprave
- Samodejna izračuna razdalje (Haversinova formula) in višinskega pridobitka
- Shranjevanje kot JSON v SharedPreferences prek `TrailRepository`

### 🤖 Gemini integracija (GeminiActivity)
- Vgrajen AI asistent za priporočila in odgovore o planinskih poteh

---

## Tehnični stack

| Komponenta | Tehnologija |
|---|---|
| Jezik | Java |
| Platforma | Android SDK |
| Kartiranje | osmdroid + OpenStreetMap |
| Mrežna komunikacija | Volley |
| Vremenski podatki | OpenWeatherMap 2.5 API |
| Radarski overlay | ARSO GIF radar |
| GPX razčlenjevanje | XmlPullParser |
| Shranjevanje | SharedPreferences (JSON) |
| Build sistem | Gradle (Kotlin DSL) |

---

## Struktura projekta

```
app/src/main/java/si.uni_lj.fe.tnuv.vzponapp/
├── MainActivity.java          # Vstopna točka, usmeritev na Onboarding ali Home
├── OnboardingActivity.java    # Uvodni zaslon z vnosom profila
├── HomeActivity.java          # Glavni zaslon s seznamom poti
├── TrailDetailsActivity.java  # Podrobnosti posamezne poti
├── MapActivity.java           # GPX zemljevid z ARSO radarjem
├── AddTrailActivity.java      # Uvoz GPX poti
├── ProfileActivity.java       # Uporabniški profil
├── GeminiActivity.java        # AI asistent
├── Trail.java                 # Podatkovni model poti
├── TrailRepository.java       # Repozitorij in persistenca poti
├── TrailCardAdapter.java      # RecyclerView adapter za kartice
├── GpxService.java            # Razčlenjevanje in predpomnjenje GPX
├── WeatherService.java        # Vreme, predpomnjenje, varnostna ocena
└── NavigationHelper.java      # Skupna bottom bar navigacija
```

---

## Namestitev

1. Kloniraj repozitorij
2. Odpri projekt v Android Studiu
3. V `WeatherService.java` nastavi svoj OpenWeatherMap API ključ
4. Zaženi na emulatorju ali fizični napravi (Android 8.0+)

---

## API ključi

Aplikacija uporablja:
- **OpenWeatherMap** – brezplačni ključ za `/data/2.5/weather` in `/data/2.5/forecast`
- **Google Gemini** – ključ za AI asistenta v `GeminiActivity.java`
- **ARSO radar** – javno dostopen, brez ključa

---

## Avtor

Razvila: **Andraž Dimc, Pia Veniger Djuras**