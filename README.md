# 📱 CareCrew

---

## 🚀 About CareCrew
*CareCrew* is an integrated maintenance and complaint management ecosystem developed natively for the Android platform. It digitizes and streamlines campus-level facility management by consolidating student grievances, staff responses, and administrative oversight into a single, transparent interface. 

By moving away from informal, untraceable communication channels like verbal reporting or unrecorded calls, CareCrew ensures every service request follows a strictly documented, auditable path from initiation to closure.

### 🏗️ Codebase Statistics
CareCrew is built with production-ready stability in mind, moving past a basic proof-of-concept into a comprehensive application framework consisting of:
*   *39 Java Source Files:* Handling backend logic, complex state transitions, and asynchronous operations.
*   *49 XML Layout Configurations:* Powering a fluid, responsive UI designed for a seamless user experience.
*   *Custom UI Elements:* Packed with bespoke drawable assets and micro-animations to polish the end-to-end interface.

---

## 🔒 Role-Based Access Control (RBAC) Architecture
The system employs a strict RBAC model to ensure that workflows are highly specialized, isolated, and secure. The application dynamically adjusts its interface based on four primary user entry points:

| Role | Primary Functions & Features |
| :--- | :--- |
| *👨‍🎓 Student (User)* | Digital submission of complaints, attaching precise location data, uploading photographic evidence, and tracking real-time status updates. |
| *🛠️ Maintenance Personnel (Staff)* | Task-oriented work order dashboard, ticket filtering by category, and mandatory "before-and-after" visual verification for accountability prior to task closure. |
| *🏢 Hostel Management (Warden)* | Specialized monitoring dashboard to oversee maintenance trends, response bottlenecks, and pending issues within specific residential blocks. |
| *👑 System Administrator (Admin)* | Central authority for global user management, automated/manual workload distribution, and the dissemination of campus-wide announcements. |

---

## 🛠️ Tech Stack & Requirements

*   *Platform:* Android (Native)
*   *Language:* Java
*   *UI/UX:* XML Layouts, Material Design Components, Custom Canvas Drawables & Animations
*   *Minimum SDK:* Android 7.0 (API Level 24) or higher

---

## 📦 Getting Started & Installation

To clone this repository and set up the project on your local machine using Android Studio:

### 1. Clone the Repository
```bash
git clone [https://github.com/DhruvNimawat/carecrew.git](https://github.com/DhruvNimawat/carecrew.git)
cd carecrew
