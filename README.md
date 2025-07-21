# 🏡 Smart Home Management System

![79d83e72-06df-439d-a53e-369b23c00fea](https://github.com/user-attachments/assets/b8f30882-8a69-45b1-9ab8-315abe73d391)

## 📘 Opis Projektu

**Smart Home Management System** to aplikacja symulująca system zarządzania inteligentnym domem. Umożliwia użytkownikom:

- kontrolę nad urządzeniami,
- tworzenie automatycznych reguł,
- monitorowanie i rejestrowanie zmian w czasie rzeczywistym.

Projekt został pierwotnie zaimplementowany jako aplikacja CLI (Command Line Interface), jednak obecna wersja wykorzystuje **graficzny interfejs użytkownika (GUI)** oparty na bibliotece **Swing**, co zapewnia bardziej intuicyjną obsługę.

---

## 🚀 Główne Funkcjonalności

### 🏠 Zarządzanie strukturą domu

- Tworzenie domów z nazwą i lokalizacją
- Dodawanie pomieszczeń z określonym typem
- Przypisywanie urządzeń do konkretnych pokoi

### 🔌 Zarządzanie urządzeniami

- Dodawanie, edycja i usuwanie urządzeń:
  - żarówki (Lightbulb)
  - termostaty (Thermostat)
  - czujniki ruchu (MotionSensor)
  - zamki (Lock)
- Przypisywanie urządzeń do konkretnych pomieszczeń

### 🎛️ Sterowanie urządzeniami

- Włączanie/wyłączanie urządzeń (ON/OFF)
- Sprawdzanie aktualnego stanu
- Grupowe sterowanie urządzeniami w obrębie pokoju

### ⚙️ Automatyczne reguły

- Tworzenie reguł opartych na warunkach logicznych (np. `jeśli temperatura < 18°C`)
- Definiowanie akcji przy użyciu wyrażeń lambda
- Obsługa warunków i akcji z wykorzystaniem `Predicate<T>` i `Consumer<T>`

### 🧪 Symulacja działania

- Symulacja odczytów z czujników (np. losowe dane z `TemperatureSensor`)
- Możliwość testowania reguł i reakcji systemu

### 📝 Logowanie zdarzeń

- Rejestrowanie zdarzeń do pliku `.tsv` z formatem:
  TIMESTAMP DEVICE_ID DEVICE_TYPE ROOM_NAME EVENT_TYPE DESCRIPTION

---

#### 👨‍👩‍👧‍👦 Dziedziczenie i Polimorfizm

- Abstrakcyjna klasa `SmartDevice` jako baza

#### 🧾 Logowanie

- Dedykowana klasa do zapisu logów w czasie rzeczywistym
- Format `TSV`, pełna zgodność i spójność danych

---

## 🖥️ Interfejs użytkownika (GUI)

Aplikacja korzysta z biblioteki **Java Swing** i oferuje:

- Przejrzysty, interaktywny interfejs graficzny
- Obsługę zdarzeń z poziomu GUI
- Szybką kontrolę i monitoring urządzeń
  
![download](https://github.com/user-attachments/assets/b2d1cd99-ec06-4eaf-9987-b991755e0507)
![download](https://github.com/user-attachments/assets/5e18a075-a63d-4304-a366-e0a6428643ef)
![download](https://github.com/user-attachments/assets/7e713767-71dc-40b8-8e50-bc62afbb998f)
![download](https://github.com/user-attachments/assets/41fc3117-3ae9-4d04-a7eb-aa52e67127a0)
![download](https://github.com/user-attachments/assets/25ee9754-453c-44b1-9c3c-639dc7abb1f8)
