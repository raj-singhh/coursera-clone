# 🎓 Coursera Clone

A simplified Coursera-like e-learning platform built with **Angular**, **Tailwind CSS**, and **Spring Boot (Java)**. This project supports user registration, course browsing, video-based learning, and certificate generation upon completion of all course content.

---

## ✨ Features

- 🔐 **User Authentication**
  - Register/Login with validation
  - Auth token handling (JWT or similar)

- 📚 **Course Listing**
  - View all available courses
  - Each course displays thumbnail, description, and purchase option

- 💳 **Purchase Logic**
  - Paid/Free course structure (optional integration with Razorpay or dummy logic)

- 🎥 **Video Lessons**
  - Course detail page with a list of videos
  - Watch tracking implemented

- 🏆 **Certificate Generation**
  - After 100% video completion, a **Download Certificate** button appears
  - Certificate is downloaded as a PDF

- 🧾 **My Courses Section**
  - Shows all purchased/enrolled courses
  - Progress tracking per course

---

## 🛠️ Tech Stack

### Frontend
- [Angular](https://angular.io/) (v16+)
- [Tailwind CSS](https://tailwindcss.com/)
- Axios / HTTPClient for API requests

### Backend
- [Spring Boot](https://spring.io/projects/spring-boot)
- Java 17+
- REST API development
- File handling for certificate generation
- Optional: PDF generation with `iText` or `OpenPDF`

---



### 📁 Project Structure

```text
coursera-clone/
├── coursera-frontend/               # Angular Frontend (Client)
│   └── src/
│       └── app/
│           ├── auth/               # Login/Register
│           ├── shared/             # Services, guards
│           └── ...                 # Other modules/components
│
├── coursera-backend/               # Spring Boot Backend (Renamed from coursera-clone)
│   ├── src/main/java/
│   │   └── com/example/coursera/
│   │       ├── controller/
│   │       ├── service/
│   │       ├── model/
│   │       └── ...                 # Other backend packages
│   └── src/main/resources/
│       ├── application.properties
│       └── static/
│           └── certificates/       # Folder where generated PDFs are stored
```


yaml
Copy
Edit

---

## 🚀 Getting Started

### Prerequisites

- Node.js (18+), Angular CLI
- Java 17+
- Maven or Gradle

---

### 🔧 Backend Setup

```bash
cd coursera-backend
./mvnw spring-boot:run
Server starts on: http://localhost:8080

💻 Frontend Setup
bash
Copy
Edit
cd coursera-frontend
npm install
ng serve
App runs on: http://localhost:4200

📜 API Endpoints (Sample)
Method	Endpoint	Description
POST	/api/auth/register	Register a new user
POST	/api/auth/login	User login
GET	/api/courses	Get all courses
GET	/api/courses/{id}	Course details with videos
POST	/api/complete/{userId}/{courseId}	Mark course as complete
GET	/api/certificate/download/{userId}/{courseId}	Download certificate PDF

📄 Certificate Sample
Automatically generated on course completion

Downloadable as a .pdf with user name, course name, and date

📦 Future Improvements
Razorpay integration for real payments

Admin panel to manage courses

Video progress tracking with percentage

Email notifications after certificate generation

Course rating and reviews

🙌 Contributing
Pull requests are welcome! Feel free to open issues for suggestions or bugs.

📃 License
This project is for educational purposes and not affiliated with Coursera Inc.

👨‍💻 Developed By
Raj Singh
B.Tech CSE, RKGIT

Passionate about full-stack development and building scalable learning platforms.

yaml
Copy
Edit

---

