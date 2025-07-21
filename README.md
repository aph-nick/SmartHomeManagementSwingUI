🏡 Smart Home Management System


![79d83e72-06df-439d-a53e-369b23c00fea](https://github.com/user-attachments/assets/b8f30882-8a69-45b1-9ab8-315abe73d391)

Opis Projektu

Projekt Smart Home Management System to aplikacja symulująca system zarządzania inteligentnym domem, umożliwiająca użytkownikom kontrolę nad urządzeniami, tworzenie automatycznych reguł oraz monitorowanie i rejestrowanie zmian. Pierwotnie projekt został zaimplementowany z interfejsem tekstowym (CLI), jednak w obecnej wersji został przebudowany z graficznym interfejsem użytkownika (GUI) opartym na bibliotece Swing, co zapewnia bardziej intuicyjną interakcję.

Główne Funkcjonalności

    Zarządzanie strukturą domu: Definiowanie domów z podstawowymi cechami (nazwa, lokalizacja) oraz dodawanie do nich wielu pomieszczeń (pokoi) z określonym typem i listą urządzeń.

    Zarządzanie urządzeniami: Dodawanie, usuwanie i edycja różnego typu urządzeń (np. światła, termostaty, czujniki ruchu, zamki) w poszczególnych pomieszczeniach.

    Sterowanie urządzeniami: Możliwość zmiany statusu urządzeń (np. ON/OFF), sprawdzania ich aktualnego stanu oraz grupowego sterowania (np. włączanie wszystkich urządzeń w danym pokoju).

    Automatyczne reguły: Tworzenie reguł automatyzacji z warunkami logicznymi (np. "jeśli temperatura < X°C") i powiązanymi z nimi akcjami, z wykorzystaniem wyrażeń lambda.

    Symulacja działania: Symulowanie zmian stanu urządzeń (np. losowe odczyty czujników temperatury).

    Logowanie zdarzeń: Rejestrowanie w czasie rzeczywistym wszystkich zdarzeń związanych ze zmianą stanu urządzeń do pliku tekstowego (TSV) z dokładnymi informacjami (TIMESTAMP, DEVICE_ID, DEVICE_TYPE, ROOM_NAME, EVENT_TYPE, DESCRIPTION). Generowanie zestawień z działania urządzeń (np. średnia temperatura, lista aktywnych urządzeń) z wykorzystaniem Stream API.

Technologia i Architektura

Projekt został zrealizowany w Javie i bazuje na obiektowym podejściu. Kluczowe elementy architektury obejmują:

    Dziedziczenie i Polimorfizm: Wykorzystanie abstrakcyjnej klasy SmartDevice jako podstawy dla wszystkich urządzeń (np. Lightbulb, Outlet, TemperatureSensor)

    Interfejsy:

        Switchable: Dla urządzeń, które mogą być włączane i wyłączane.

        SensorDevice<T>: Dla czujników, dostarczających dane generycznego typu (np. TemperatureSensor).

        ObservableDevice i DeviceObserver: Implementacja wzorca obserwatora do powiadamiania o zmianach stanu urządzeń.

    Funkcyjne interfejsy: Wykorzystanie Predicate<T> i Consumer<T> do definiowania warunków i akcji w klasie Rule.

    Logowanie: Dedykowana klasa do obsługi zapisu logów do pliku TSV, zapewniająca spójny format i aktualizację w czasie rzeczywistym.

    Interfejs Użytkownika (GUI): Aplikacja posiada graficzny interfejs użytkownika zbudowany w oparciu o bibliotekę Java Swing, co pozwala na interaktywną obsługę systemu.

![download](https://github.com/user-attachments/assets/b2d1cd99-ec06-4eaf-9987-b991755e0507)
![download](https://github.com/user-attachments/assets/5e18a075-a63d-4304-a366-e0a6428643ef)
![download](https://github.com/user-attachments/assets/7e713767-71dc-40b8-8e50-bc62afbb998f)
![download](https://github.com/user-attachments/assets/41fc3117-3ae9-4d04-a7eb-aa52e67127a0)
![download](https://github.com/user-attachments/assets/25ee9754-453c-44b1-9c3c-639dc7abb1f8)
