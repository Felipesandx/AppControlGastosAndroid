# AppControlGastos - Android Client 📱💸

![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg)
![Android Studio](https://img.shields.io/badge/Android%20Studio-Hedgehog+-green.svg)
![Room](https://img.shields.io/badge/Room-Database-yellow.svg)

Aplicación nativa de Android diseñada para el control, registro y análisis de finanzas personales. Este repositorio contiene exclusivamente la **capa de presentación y persistencia de datos local**, consumiendo toda la lógica de negocio y reglas financieras desde nuestro módulo independiente `Core`.

## 🌟 Características Principales

*   **Multiusuario Local:** Soporte para múltiples perfiles gracias al gestor de sesiones (`UserSessionManager` y `UserSelectionActivity`).
*   **Gestión Rápida de Gastos:** Interfaz fluida para añadir nuevas transacciones (`AddTransactionActivity`), categorizarlas e identificar si son ingresos o egresos.
*   **Panel Analítico:** Visualización de resúmenes financieros mensuales y categorizados (`AnalyticsActivity`).
*   **Persistencia Robusta:** Uso de **Room Database** para almacenar todas las transacciones de forma segura y eficiente en el dispositivo.
*   **Experiencia de Usuario (UX):** Transiciones suaves mediante animaciones personalizadas (`slide_in_bottom.xml`, `slide_out_bottom.xml`).

## 🏗 Arquitectura y Patrones de Diseño

El proyecto está diseñado bajo el patrón **MVVM (Model-View-ViewModel)** y se integra con los principios de **Clean Architecture**:

*   **UI Layer (Vistas):** Construida con XML tradicional de Android. Utiliza `RecyclerView` con `TransactionAdapter` para listados eficientes.
*   **Presentation Layer (ViewModels):** Archivos como `AnalyticsViewModel` mantienen el estado de la UI y sobreviven a los cambios de configuración (como la rotación de pantalla).
*   **Data Layer:** Implementa el patrón Repositorio (`TransactionRepositoryRoom`) que sirve de puente entre los DAOs de Room y los casos de uso del módulo Core.

## 📂 Estructura del Proyecto

```text
app/src/main/
├── java/edu/umg/appcontrolgastos/
│   ├── data/                 # Room Database, Entidades y DAOs
│   ├── AddTransactionActivity.kt # Pantalla de registro
│   ├── AnalyticsActivity.kt  # Pantalla de reportes
│   ├── ControlGastosApp.kt   # Clase Application para inyección/configuración
│   ├── MainActivity.kt       # Dashboard principal
│   └── UserSessionManager.kt # Gestión de la sesión actual
└── res/                      # Recursos de Android
    ├── anim/                 # Animaciones de transiciones
    ├── drawable/             # Vectores y gráficos
    ├── layout/               # Diseños XML (UI)
    └── values/               # Colores, strings, temas (Soporte Dark Mode)
```

## 📋 Requisitos Previos

*   **Android Studio:** Versión Giraffe, Hedgehog o superior.
*   **JDK:** Java 17 o superior.
*   **Módulo Core:** Este proyecto requiere que el módulo `AppControlGastos-Core` esté disponible (ya sea publicado en Maven Local o como proyecto incluido en Gradle).

## 🚀 Instalación y Ejecución

1.  **Clona el repositorio:**
    ```bash
    git clone https://github.com/tu-usuario/AppControlGastosAndroid.git
    ```
2.  **Configura el Core:**
    Asegúrate de haber compilado previamente el módulo Core utilizando Maven (`mvn clean install`) para que Gradle pueda resolver las dependencias, o inclúyelo en tu `settings.gradle.kts`.
3.  **Sincroniza y Ejecuta:**
    Abre el proyecto en Android Studio, haz clic en *Sync Project with Gradle Files*, y luego presiona **Run** (`Shift + F10`) en un emulador o dispositivo físico.