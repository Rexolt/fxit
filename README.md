# f(xit) Fügvény ábrázoló

**Ez a projekt egy interaktív grafikonrajzoló alkalmazás,** amely több matematikai és vizualizációs funkciót kínál. A programban függvények bevitele, rajzolása, animálása, deriváltak és integrálok számítása, valamint extra eszközök (például poligonmérés, marker és jegyzetelés) érhetők el.
Főbb funkciók

### Függvény rajzolás:

    Támogatottak a hagyományos függvények, sőt polár függvények is (az "isPolar" opció bekapcsolásával).

### Animáció:

    Az alkalmazás képes animálni olyan függvényeket, amelyekben szerepel a t változó. Az animáció során a t értéke folyamatosan változik, így a függvény grafikonja dinamikusan módosul.

### Derivált és tangent vonal:

    Derivált számítása kis numerikus lépéssel, illetve a tangent vonal kirajzolása az adott pontban.

### Integrálás:

    Az integrál értékének számítása adott intervallumon, valamint a terület árnyékolása a grafikonon.

### Extra eszközök:

    Marker eszköz, poligon mérés (terület, kerület számítása), valamint egy beépített számológép.

### Jegyzetelés:

    Külön jegyzet panel áll rendelkezésre, ahol a felhasználó választhat két mód közül:
        Plain mód: Egyszerű szövegszerkesztő, egyenletszerkesztő gombbal.
        Markdown mód: Támogatja a Markdown szintaxist (dőlt, félkövér, címsorok) és LaTeX-szerű matematikai jelöléseket.

### Telepítés és futtatás

### Követelmények:
    - Java 8 vagy újabb
    - Kotlin (ha Kotlin-ban fejlesztetted a kódot)
    - Az exp4j könyvtár (a függvények kiértékeléséhez)

### Fordítás és futtatás:
        - Klónozd a repót, majd nyisd meg a kedvenc IDE-dben (IntelliJ IDEA javasolt).
        - Győződj meg róla, hogy az összes szükséges függőség (pl. exp4j) elérhető.
        - Futtasd a main() függvényt a MainFrame osztályban.

### Használat

### Függvények bevitele:
A bal oldali fülön, a "Függvények" lapon található panelen add meg a függvény képletét, a domain határait, válaszd ki a színt, vonalvastagságot, illetve a polár módot, ha szükséges.

### Számol & Rajzol:
A "Számol & Rajzol" gomb megnyomása után a program kirajzolja a függvényeket, kiszámolja a zérushelyeket, metszéspontokat, stb.

### Animáció:
Ha a függvény képletében szerepel a t változó, az "Animate" gomb megnyomásával elindíthatod az animációt. A gomb újra megnyomásával leállíthatod.

### Jegyzetelés:
A "Jegyzetek" fülön választhatsz a Plain vagy Markdown mód között. Itt jegyzetelhetsz, illetve beilleszthetsz egyenleteket az egyenletszerkesztő segítségével. A jegyzeteket elmentheted vagy betöltheted.

### Extra funkciók:
A "Extra funkciók" panelen elérhetők a számológép, derivált számítás, integrál számítás, marker eszköz és egyéb extra műveletek.

### Testreszabás

A beállítások dialógusban módosíthatod a grafikon megjelenését (pl. rács, tengelyek, háttérszín, stb.).
    A bal oldali panelen a függvény hozzárendelési szabályát is megadhatod, így az alapértelmezett képlet könnyen módosítható.

### Fejlesztési ötletek

    További matematikai függvények hozzáadása az exp4j-hoz.
    Részletesebb Markdown támogatás, például MathJax integráció a teljes LaTeX támogatáshoz.
    Exportálási lehetőségek kiterjesztése (pl. SVG vagy PDF formátum).

### Kapcsolat

Ha kérdésed, javaslatod vagy hibajelentésed van, kérlek nyisd meg a GitHub Issues részt a projekt repójában, vagy írj nekem a megadott elérhetőségeken.