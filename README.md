# Literary Emporium — Online Book Marketplace

An online marketplace for books featuring multi-role access (Buyer, Seller, Administrator), complete catalog browsing, secure user authentication, shopping cart, order placement, and inventory management.

## 🏛️ System Architecture

- **Frontend**: HTML5, CSS3, JavaScript (Responsive, Multi-lingual UI)
- **Backend**: Java 21, Jakarta Servlets 6.0, Jakarta REST APIs
- **Database**: MySQL 8.0 with JDBC (MySQL Connector/J 9.0)
- **Build Tool**: Apache Maven 3.9
- **Server**: Apache Tomcat 10.1
- **JSON Serialization**: Google Gson 2.11
- **Security**: SHA-256 Password Hashing, PreparedStatement SQL protection

```
Browser (HTML/CSS/JS) 
       ↕ (HTTP / JSON)
Jakarta Servlets (Auth, Books, Cart, Orders)
       ↕ 
DAO Layer (UserDAO, BookDAO, CartDAO, OrderDAO)
       ↕ 
JDBC (DBConnection)
       ↕ 
MySQL Database (literary_emporium)
```

## 👥 User Roles & Features

1. **Buyer**:
   - Browse books by category (Fiction, Non-Fiction, Academic, Biography).
   - Real-time search and book details view.
   - Shopping cart and wishlist management.
   - Checkout and order placement with order history.

2. **Seller**:
   - Manage listed books and inventory stock.
   - Add new books with pricing and description.
   - View order status.

3. **Administrator**:
   - Manage marketplace users and registered sellers.
   - Oversee inventory and monitor orders.

## 🚀 Setup & Execution

### 1. Database Setup
1. Open MySQL Workbench or MySQL CLI.
2. Run `schema.sql` to create database tables and insert sample records:
   ```sql
   source schema.sql;
   ```

### 2. Build Backend with Maven
In `Backend` directory:
```bash
mvn clean package
```
This generates `target/literary-emporium.war`.

### 3. Deploy to Apache Tomcat
1. Copy `literary-emporium.war` to Tomcat's `webapps/` folder.
2. Start Tomcat using:
   ```powershell
   catalina.bat run
   ```
3. Open in browser: `http://localhost:8080/literary-emporium/`

## 🔑 Default Credentials (for testing)

- **Admin**: `admin@literaryemporium.com` / `admin123`
- **Seller**: `seller@literaryemporium.com` / `seller123`
- **Buyer**: `buyer@literaryemporium.com` / `buyer123`

---
© 2026 Literary Emporium. College Capstone Project.
