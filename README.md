# README – VZPON aplikacija

Aplikacija za pomoč pohodnikom pri izbiri primerne ture glede na vreme, zahtevnost in uporabniške izkušnje.
Ideja in prototipi: 

---

# TODO CHECKLIST

## 1. Onboarding / Welcome screen

* [ ] Background slika gora
* [ ] Naslov “Pozdravljeni!”
* [ ] Input za ime uporabnika
* [ ] Gumb “Naprej”
* [ ] Shrani ime uporabnika (SharedPreferences)
* [ ] Prehod na naslednji screen

---

## 2. Izbira izkušenj uporabnika

* [ ] Radio buttoni:

  * [ ] Profesionalec
  * [ ] Rekreativec
  * [ ] Začetnik
* [ ] Označevanje izbrane možnosti
* [ ] Gumb “Naprej”
* [ ] Shrani stopnjo izkušenj
* [ ] Navigacija naprej

---

## 3. Loading / vreme screen

* [ ] Loading indikator
* [ ] Besedilo “Nalagam napoved...”
* [ ] Klic ARSO API-ja
* [ ] Prenos vremenskih podatkov
* [ ] Obdelava JSON podatkov
* [ ] Error handling če ni interneta
* [ ] Prehod na home screen

---

# HOME SCREEN

## 4. Zemljevid Slovenije

* [ ] Prikaz Google Maps / OpenStreetMap
* [ ] Markerji regij/poti
* [ ] Barvni indikator:

  * [ ] Varno
  * [ ] Previdno
  * [ ] Nevarno
* [ ] Klik na marker
* [ ] Premik po zemljevidu
* [ ] Zoom

---

## 5. Seznam priporočenih poti

* [ ] Card view poti
* [ ] Slika poti
* [ ] Ime poti
* [ ] Razdalja
* [ ] Zahtevnost
* [ ] Vremenski indikator
* [ ] Scroll seznam
* [ ] Klik na kartico

---

## 6. Filter screen

* [ ] Slider za dolžino poti
* [ ] Radio buttoni za zahtevnost
* [ ] Filtri glede vremena
* [ ] Izbira regije
* [ ] Gumb “Uporabi filtre”
* [ ] Filtriranje seznama poti

---

# PODROBNOSTI POTI

## 7. Detail screen poti

* [ ] Velika slika poti
* [ ] Ime poti
* [ ] Opis poti
* [ ] Dolžina
* [ ] Višinska razlika
* [ ] Zahtevnost
* [ ] Trenutno vreme
* [ ] GPX prikaz
* [ ] Gumb za shranjevanje ture

---

## 8. GPX / karta poti

* [ ] Nalaganje GPX datoteke
* [ ] Izris trase na zemljevidu
* [ ] Začetna in končna točka
* [ ] Zoom na traso
* [ ] Prikaz dolžine trase

---

## 9. Priporočena oprema

* [ ] Seznam priporočene opreme
* [ ] Priporočila glede vremena
* [ ] Priporočila glede zahtevnosti
* [ ] Ikone opreme
* [ ] Dinamično prilagajanje priporočil

---

# PROFIL

## 10. Profil uporabnika

* [ ] Profilna slika
* [ ] Ime uporabnika
* [ ] Stopnja izkušenj
* [ ] Dropdown menuji
* [ ] Galerija slik
* [ ] Urejanje podatkov
* [ ] Shranjevanje sprememb

---

# SHRANJEVANJE PODATKOV

## 11. SharedPreferences

* [ ] Shrani ime uporabnika
* [ ] Shrani izkušnje
* [ ] Shrani nastavitve filtrov
* [ ] Shrani priljubljene poti

---

# NAVIGACIJA

## 12. Navigation

* [ ] Bottom navigation bar
* [ ] Navigacija med screeni
* [ ] Back button
* [ ] Intenti med aktivnostmi

---

# UI / DESIGN

## 13. Dizajn aplikacije

* [ ] Material3 tema
* [ ] Custom barve
* [ ] Rounded buttons
* [ ] Konsistentni spacingi
* [ ] Responsive layout
* [ ] Temni način (optional)

---

# DODATNO

## 14. Debugging in testiranje

* [ ] Test na emulatorju
* [ ] Test na telefonu
* [ ] Logcat debugging
* [ ] Preverjanje lifecycle metod
* [ ] Internet permissions

---

# UPORABLJENE TEME IZ VAJ

* Aktivnosti in UI 
* Intenti in navigacija 
* Lifecycle aktivnosti 
* SharedPreferences / shranjevanje 
* Prenos podatkov in API 
