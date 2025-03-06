# f(xit) Fügvény ábrázoló

**Figyelem!**  
A projektben hamarosan újabb, nagyobb volumenű update-ok fognak megjelenni!  
*Például: Geometriai ábrázoló, web verzió javítása.*

---

## Leírás

Ez a projekt egy **interaktív grafikonrajzoló alkalmazás**, amely számos matematikai és vizualizációs funkcióval rendelkezik. Az alkalmazás segítségével:
- Beviheted, rajzolhatod és animálhatod a függvényeket.
- Számíthatod a deriváltakat és integrálokat.
- Használhatod az extra eszközöket, mint például poligonmérés, marker és jegyzetelés.

---

## Főbb Funkciók

### Függvény Rajzolás
- **Támogatottak a hagyományos függvények, sőt polár függvények is**  
  *(Az "isPolar" opció bekapcsolásával.)*

### Animáció
- **Dinamikus animáció:**  
  Az alkalmazás képes animálni olyan függvényeket, amelyekben szerepel a `t` változó. Az animáció során a `t` értéke folyamatosan változik, így a grafikon dinamikusan módosul.

### Derivált és Tangent Vonalk
- **Numerikus derivált számítása:**  
  Kis lépésekben számolja a deriváltat, és kirajzolja a tangent vonalat az adott pontban.

### Integrálás
- **Integrál értékének számítása:**  
  Meghatározza az integrál értékét egy adott intervallumon, valamint árnyékolja a területet a grafikonon.

### Extra Eszközök
- **Marker, poligonmérés és beépített számológép:**  
  Segítségével terület- és kerületszámítást végezhetsz, illetve extra műveleteket hajthatsz végre.

### Jegyzetelés
- **Külön jegyzet panel:**  
  Itt két mód közül választhatsz:
    - **Plain mód:** Egyszerű szövegszerkesztő egyenletszerkesztő gombbal.
    - **Markdown mód:** Támogatja a Markdown szintaxist *(dőlt, félkövér, címsorok)* és LaTeX-szerű matematikai jelöléseket.

---

## Telepítés és Futtatás

### Követelmények
- **Java 8 vagy újabb**
- **Kotlin** *(amennyiben Kotlin-ban fejlesztetted a kódot)*
- **exp4j könyvtár** *(a függvények kiértékeléséhez)*

### Fordítás és Futtatás
1. **Klónozd a repót.**
2. **Nyisd meg a projektet a kedvenc IDE-dben** *(IntelliJ IDEA javasolt)*.
3. **Ellenőrizd a függőségeket:** Győződj meg róla, hogy minden szükséges függőség *(pl. exp4j)* elérhető.
4. **Futtasd a programot:** Indítsd el a `main()` függvényt a `MainFrame` osztályban.

---

## Használat

### Függvények Bevite
- A bal oldali fülön, a **"Függvények"** lapon található panelen add meg:
    - A függvény képletét.
    - A domain határait.
    - A kívánt színt, vonalvastagságot.
    - Szükség esetén a polár mód aktiválását.

### Számol & Rajzol
- A **"Számol & Rajzol"** gomb megnyomása után az alkalmazás kirajzolja a függvényeket, kiszámolja a zérushelyeket, metszéspontokat, stb.

### Animáció
- Ha a függvény képletében szerepel a `t` változó, az **"Animate"** gomb segítségével elindíthatod az animációt. A gomb ismételt megnyomásával leállíthatod az animációt.

### Jegyzetelés
- A **"Jegyzetek"** fülön választhatsz a **Plain** vagy **Markdown** mód között:
    - **Plain mód:** Egyszerű jegyzetelés.
    - **Markdown mód:** Kiterjedt formázási lehetőségek (Markdown és LaTeX-szerű jelölések) használata.
- Jegyzeteidet elmentheted vagy betöltheted későbbi munkához.

### Extra Funkciók
- Az **"Extra funkciók"** panelen elérhetők:
    - Beépített számológép.
    - Derivált számítás.
    - Integrál számítás.
    - Marker eszköz és egyéb extra műveletek.

---

## Testreszabás

- **Grafikon beállítások:**  
  A beállítások dialógusban módosíthatod a grafikon megjelenését, például a rácsot, tengelyeket, háttérszínt, stb.
- **Függvény hozzárendelési szabály:**  
  A bal oldali panelen megadhatod a függvény alapértelmezett képletét, így könnyen testreszabhatod a megjelenést.

---

## Fejlesztési Ötletek

- Új matematikai függvények hozzáadása az **exp4j**-hoz.
- Részletesebb **Markdown támogatás**: Például MathJax integráció a teljes LaTeX támogatás érdekében.
- Exportálási lehetőségek kiterjesztése, mint például **SVG** vagy **PDF** formátum.

---

## Kapcsolat

Ha kérdésed, javaslatod vagy hibajelentésed van, kérlek:
- Nyisd meg a **GitHub Issues** részt a projekt repójában, vagy
- Vedd fel velem a kapcsolat a megadott elérhetőségeken.

---


