---
title: "Sorfejtések: Taylor-sorok és Fourier-sorok"
tags:
  - matematika
  - analízis
  - sorfejtések
created: 2025-02-17
Author: Rexolt
---

# Sorfejtések: Taylor-sorok és Fourier-sorok

Ebben a fejezetben a függvények sorfejtéseinek világába merülünk el. Megismerkedünk a Taylor-sor és a Fourier-sor alapjaival, amelyek segítségével a függvényeket végtelen polinomok vagy hullámok formájában közelíthetjük.

---

## Taylor-sor

A Taylor-sor egy olyan sorfejtési módszer, amely egy függvényt egy adott pont körül végtelen polinomként fejez ki. Legyen $f(x)$ egy analitikus függvény, és legyen $a$ egy adott pont. Ekkor a Taylor-sor a következőképpen írható fel:

 $f(x) = \sum_{n=0}^{\infty} \frac{f^{(n)}(a)}{n!} (x-a)^n$ 

ahol $f^{(n)}(a)$ a függvény $n$-edik deriváltja az $a$ pontban, és $n!$ a faktoriális.

### Példa

Vegyük a $e^x$ függvényt, melynek minden deriváltja $e^x$. Így a Taylor-sor $a=0$ körül:

 $e^x = \sum_{n=0}^{\infty} \frac{x^n}{n!}$ 

Ez azt jelenti, hogy $e^x$ végtelen polinomként is kifejezhető, és ezzel a sorral $e^x$ értékei közelíthetők.

---

## Fourier-sor

A Fourier-sor a periodikus függvények hullámkomponensek szerinti felbontását teszi lehetővé. Egy periodikus függvény, amelynek periódusa $2\pi$, a következőképpen írható fel Fourier-sorként:

 $f(x) = \frac{a_0}{2} + \sum_{n=1}^{\infty} \Bigl( a_n \cos(nx) + b_n \sin(nx) \Bigr)$ 

ahol az együtthatók a következő képletekkel számíthatók:

 $a_n = \frac{1}{\pi} \int_{-\pi}^{\pi} f(x) \cos(nx)\, dx$ 

 $b_n = \frac{1}{\pi} \int_{-\pi}^{\pi} f(x) \sin(nx)\, dx$ 

### Példa

Vegyük például a négyszög hullámfüggvényt. Fourier-sor segítségével a négyszög hullámot végtelen számban harmonikus (szinuszos és koszinuszos) függvény összegeként fejezhetjük ki, így egyre jobb közelítést érve el a hullám diszkrét komponenseire.

---

## Összegzés

A sorfejtések – legyen szó Taylor- vagy Fourier-sorokról – alapvető eszközei a függvények analízisének. A Taylor-sor segítségével a függvények helyi viselkedését, míg a Fourier-sorral a periodikus jelenségek összetevőit tudjuk vizsgálni. Ezek az eszközök számos matematikai és mérnöki alkalmazásban nélkülözhetetlenek, legyen szó numerikus közelítésről, jelanalízisről vagy differenciálegyenletek megoldásáról.

A következő fejezetben más analízis módszerekről is beszélünk, például a sorok konvergenciájáról és a hibahatár számításáról.


[[Differenciálegyenletek]]