---
title: Deriválás és Integrálás
tags:
  - matematika
  - analízis
  - deriválás
  - integrálás
created: 2025-02-17
Author: Rexolt
---

# Deriválás és Integrálás

Ebben a fejezetben megismerkedünk a függvények analízisének két alapvető eszközével: a **deriválással** és az **integrálással**. Ezek a műveletek lehetővé teszik, hogy mélyebben megértsük a függvények viselkedését, és számos gyakorlati problémát oldjunk meg a matematikában, fizikában, vagy mérnöki alkalmazásokban.

---

## Deriválás

### Mi az a derivált?

A derivált a függvény lokális változását méri. Intuitív értelemben a derivált azt mutatja meg, hogy egy függvény mennyire „meredeken” emelkedik vagy süllyed egy adott pontban. Matematikailag a derivált definíciója a következő:


$f'(x) = \lim_{h \to 0} \frac{f(x+h) - f(x)}{h}$


Ez az érték a függvény érintőjének meredekségét adja meg az \(x\) pontban.

### Alapvető szabályok

Néhány fontos deriválási szabály:

- **Hatványfüggvény deriváltja:**

  
  $\frac{d}{dx}\, x^n = n\, x^{n-1}$
  

- **Összeadás és kivonás szabálya:**


  $\frac{d}{dx}[f(x) \pm g(x)] = f'(x) \pm g'(x)$


- **Szorzatszabály:**


  $\frac{d}{dx}[f(x)g(x)] = f'(x)g(x) + f(x)g'(x)$


- **Hányadosszabály:**


  $\frac{d}{dx}\left[\frac{f(x)}{g(x)}\right] = \frac{f'(x)g(x) - f(x)g'(x)}{[g(x)]^2}$


- **Láncszabály (kompozíció deriválása):**


  $\frac{d}{dx}\, f(g(x)) = f'(g(x)) \cdot g'(x)$


### Példa

Vegyük a következő függvényt:


$f(x) = x^2 + 3x + 2$


A derivált kiszámítása során:


$f'(x) = 2x + 3$


Ez azt jelenti, hogy például \(x = 1\)-nél a függvény érintőjének meredeksége:


$f'(1) = 2\cdot1 + 3 = 5$

---

## Integrálás

### Mi az az integrál?

Az integrálás a deriválás inverz művelete, amely két fő formában jelenik meg:

- **Határozatlan integrál:** Egy függvény primitív függvényét adja meg, azaz


  $\int f(x)\, dx = F(x) + C$


  ahol \( $F'(x)=f(x)$ \) és \(C\) az integrálási állandó.

- **Határozott integrál:** Az integrálás segítségével meghatározható a függvény által lefedett „terület” egy adott intervallumon belül:


  $\int_a^b f(x)\, dx = F(b) - F(a)$


### Alapvető integrálási szabályok

Néhány alapvető szabály:

- **Hatványfüggvény integrálja:**


  $\int x^n\, dx = \frac{x^{n+1}}{n+1} + C \quad (n \neq -1)$


- **Exponenciális függvény integrálja:**


  $\int e^x\, dx = e^x + C$


- **Trigonometrikus függvények integráljai:**


  $\int \sin x\, dx = -\cos x + C,\quad \int \cos x\, dx = \sin x + C$


### Példa

Számoljuk ki a következő határozott integrált:


$\int_0^1 (3x^2 + 2x + 1)\, dx$


1. Első lépésként találjuk meg a primitív függvényt:


   $F(x) = x^3 + x^2 + x + C$


2. Ezután alkalmazzuk a határozott integrál definícióját:


   $\int_0^1 (3x^2 + 2x + 1)\, dx = F(1) - F(0) = (1 + 1 + 1) - 0 = 3$


---

## Deriválás és Integrálás Kapcsolata

Az analízis alapvető tétele kimondja, hogy ha \(F(x)\) egy primitív függvénye \(f(x)\)-nek, akkor:


$\frac{d}{dx}\int_a^x f(t)\, dt = f(x)$


Ez az összefüggés adja meg a deriválás és integrálás közötti szoros kapcsolatot, mely kulcsfontosságú a matematika számos ágában.

---

## Alkalmazások

- **Fizikai problémák:** Például egy test sebességének és gyorsulásának meghatározása.
- **Gazdasági modellek:** Növekedési folyamatok, mint a kamatos kamat számítása.
- **Mérnöki számítások:** Terhelések, hullámok és egyéb dinamikus rendszerek elemzése.

---

Ezzel a fejezettel megismerkedtünk a függvények analízisének két legfontosabb eszközével, a deriválással és az integrálással. A következő fejezetben részletesebben foglalkozunk a függvények viselkedésének vizsgálatával, optimalizációs problémákkal és a görbék elemzésével.


[[Görbék Elemzése és Optimalizáció]]