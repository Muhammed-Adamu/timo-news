# TimoNews

A full-stack news website with a **REST API back end and a pure HTML/JavaScript front
end**. Readers browse featured, breaking and trending stories, filter by category, search
with live suggestions, comment and like articles. Administrators manage articles,
categories and view site statistics from a dashboard.

Built with Jakarta EE Servlets on Tomcat 10, MySQL 8 and vanilla JavaScript — no
server-side templating, no front-end framework.

---

## Features

### For readers

- **Homepage** — featured, breaking, trending and latest article sections
- **Categories** — browse all eight categories with per-category article listings and counts
- **Search** — full-text search with an **autocomplete** endpoint
- **Article detail** — rendered content, view counts, like toggle and threaded comments
- **Accounts** — register, log in, edit your profile, change your password
- **Liked articles** — a personal list of everything you've liked
- **Contact form** — messages persisted to the database for administrators to read
- **Light and dark themes** — toggle persisted client-side

### For administrators

- **Dashboard** — headline statistics for articles, users, comments and likes
- **Article management** — create, edit, publish and delete articles, with draft support
- **Category management** — create, edit and delete categories, each with an icon and colour
- **Moderation** — comment and contact-message review

### System-wide

- **Front-end / back-end separation** — a single `ApiServlet` serves every `/api/*`
  endpoint as JSON; the browser never sees a server-rendered page
- **Session-based authentication** — `HttpSession` with an `ADMIN` / `USER` role model
- **BCrypt password hashing** — via `at.favre.lib:bcrypt`, cost factor 12
- **Authentication filter** — guards `/admin/*`, returning JSON `401` for API calls and
  redirecting page requests to the login screen
- **UTF-8 encoding filter** applied to every request and response
- **Session and request listeners** — track active sessions and log every HTTP request
- **Pagination** — shared `PageInfo` model across list endpoints
- **JSON serialisation** — Gson, with typed response envelopes

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 11 |
| Web API | Jakarta Servlet 5.0 (Jakarta EE 9) |
| Servlet container | Apache Tomcat 10.1 |
| Database | MySQL 8.0 |
| Persistence | Plain JDBC with prepared statements |
| JSON | Gson 2.10.1 |
| Security | BCrypt (`at.favre.lib:bcrypt` 0.10.2), `HttpSession` |
| Front end | Vanilla JavaScript, Bootstrap 5 (bundled), Bootstrap Icons |
| Build | Maven (WAR packaging) |
| Deployment | Docker on Render, MySQL on Aiven |

> **Tomcat 10 or newer is required.** The application uses the `jakarta.*` namespace;
> Tomcat 9 and earlier ship `javax.*` and cannot run it.

---

## Screenshots

| Homepage | Article detail |
|---|---|
| _screenshot_ | _screenshot_ |

| Admin dashboard | Admin article editor |
|---|---|
| _screenshot_ | _screenshot_ |

> To add these: create a `docs/screenshots/` folder, drop your PNGs in, and replace the
> placeholders with `![alt text](docs/screenshots/your-file.png)`.

---

## Getting Started

### Prerequisites

- JDK 11 or 17
- Maven 3.8+
- Apache Tomcat 10.1
- MySQL 8 running on `localhost:3306`

### 1. Create the database

The repository ships the full schema and a set of demo data.

```bash
mysql -u root -p < database/init.sql
mysql -u root -p < database/patch-more-articles.sql
```

`database/init.sql` creates the `timo_news` database, its six tables and the seed data.
`database/patch-more-articles.sql` adds articles to the categories that start empty.

> Upgrading an existing installation instead? `database/patch-fix-passwords.sql` resets
> the demo password hashes without touching any data.

### 2. Configure the database password

Credentials are read from **environment variables** — no secrets are stored in the
repository.

```java
DB_URL      // defaults to jdbc:mysql://localhost:3306/timo_news?...
DB_USER     // defaults to root
DB_PASSWORD // no default
```

`DB_URL` and `DB_USER` have sensible local defaults, so you only need to supply the
password.

**Linux / macOS / Git Bash:**

```bash
export DB_PASSWORD="your-mysql-password"
```

**Windows (PowerShell)** — sets it permanently for your user:

```powershell
[Environment]::SetEnvironmentVariable('DB_PASSWORD','your-mysql-password','User')
```

Alternatively, copy the template and fill it in:

```bash
cp src/main/resources/db.properties.example src/main/resources/db.properties
```

> `db.properties` is gitignored. `DB_PASSWORD` deliberately has **no default** — if it
> isn't set the application fails with a clear message rather than silently attempting a
> blank password.

### 3. Build

```bash
mvn clean package
```

This produces `target/timo-news.war`.

### 4. Deploy

Copy the WAR into Tomcat and start it:

```bash
cp target/timo-news.war "$CATALINA_HOME/webapps/"
"$CATALINA_HOME/bin/catalina.sh" start     # catalina.bat on Windows
```

Open <http://localhost:8080/timo-news/>.

### Demo accounts

All seed accounts share the password **`admin123`**.

| Username | Role |
|---|---|
| `admin` | ADMIN |
| `john_reader` | USER |
| `jane_author` | USER |
| `mike_editor` | USER |

---

## Project Structure

```
src/main/java/com/news/
├── dao/          JDBC data access, one class per table
├── filter/       EncodingFilter, AuthenticationFilter
├── listener/     SessionListener, RequestLoggingListener
├── model/        POJOs — News, Category, User, Comment, Contact, PageInfo
├── servlet/      ApiServlet (all /api/* JSON routes) + page servlets
└── util/         DBUtil (connection handling), StringUtil (slugs, escaping)

src/main/webapp/
├── index.html    Public homepage
├── pages/        Article detail, categories, search, login, register, profile, contact
├── admin/        Dashboard, article editor, category manager
├── assets/       CSS, JavaScript and bundled Bootstrap
└── WEB-INF/      web.xml — filter, listener and servlet registration

database/         Schema, seed data and patch scripts
docker/           Container entrypoint
Dockerfile        Render deployment image
render.yaml       Render Blueprint
```

---

## API Reference

All endpoints are served by `ApiServlet` under the application context path.

| Method | Endpoint | Auth | Purpose |
|---|---|---|---|
| `GET` | `/api/health` | — | Liveness check |
| `GET` | `/api/news` | — | Paginated article list |
| `GET` | `/api/news/featured` | — | Featured articles |
| `GET` | `/api/news/breaking` | — | Breaking-news articles |
| `GET` | `/api/news/trending` | — | Trending articles |
| `GET` | `/api/news/{id}` | — | Single article |
| `GET` | `/api/news/{id}/comments` | — | Comments for an article |
| `POST` | `/api/news/{id}/comments` | USER | Add a comment |
| `POST` | `/api/news/{id}/like` | USER | Toggle a like |
| `GET` | `/api/categories` | — | All categories with article counts |
| `GET` | `/api/search?q=` | — | Search articles |
| `GET` | `/api/search/autocomplete?q=` | — | Search suggestions |
| `POST` | `/api/auth/register` | — | Create an account |
| `POST` | `/api/auth/login` | — | Log in (creates the session) |
| `POST` | `/api/auth/logout` | USER | Log out |
| `GET` | `/api/auth/me` | USER | Current user |
| `POST` | `/api/auth/update-profile` | USER | Edit profile |
| `POST` | `/api/auth/change-password` | USER | Change password |
| `GET` | `/api/auth/liked-articles` | USER | Liked articles |
| `POST` | `/api/contact` | — | Submit a contact message |
| `GET`/`POST`/`PUT`/`DELETE` | `/api/admin/news` | ADMIN | Article CRUD |
| `GET`/`POST`/`PUT`/`DELETE` | `/api/admin/categories` | ADMIN | Category CRUD |
| `GET` | `/api/admin/stats` | ADMIN | Dashboard statistics |

---

## Data Model

Six tables with foreign keys and cascading deletes:

- **`users`** — account, role (`USER` / `ADMIN`), BCrypt password hash
- **`categories`** — name, slug, description, icon, colour, sort order
- **`news`** — article with slug, content, author and category, status
  (`DRAFT` / `PUBLISHED`), view count and featured/breaking flags
- **`comments`** — belongs to a news article and a user
- **`likes`** — unique per `(news_id, user_id)` pair
- **`contacts`** — contact-form submissions with a read flag

---

## Configuration Reference

| Variable | Default | Notes |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/timo_news?...` | Full JDBC URL |
| `DB_USER` | `root` | Database user |
| `DB_PASSWORD` | *(none)* | Required — no fallback |
| `PORT` | `8080` | Container port; rewritten into Tomcat's connector at startup |
| `CATALINA_OPTS` | `-Xmx384m` | JVM options for the container |

---

## Deployment

The repository ships a `Dockerfile` and a `render.yaml` Blueprint.

### Why a container is required

Render has no native Java runtime, and a `.war` must be served by a servlet container.
The image builds the WAR with Maven on JDK 17, then runs it on
`tomcat:10.1-jdk17-temurin` deployed as `ROOT.war`, so the site is served at the domain
root. A small entrypoint rewrites Tomcat's HTTP connector from `8080` to Render's
`$PORT`.

### 1. Create a MySQL database

Aiven's free MySQL plan works. Note that Aiven **requires TLS** — the connection string
must include `sslMode=REQUIRED`.

Load the schema:

```bash
mysql --host <HOST> --port <PORT> --user avnadmin --password \
      --ssl-mode=REQUIRED < database/init.sql
mysql --host <HOST> --port <PORT> --user avnadmin --password \
      --ssl-mode=REQUIRED < database/patch-more-articles.sql
```

### 2. Create the Render web service

Push the repository to GitHub, then in Render choose **New → Blueprint** and select it.
`render.yaml` is detected automatically and prompts for:

| Variable | Value |
|---|---|
| `DB_URL` | `jdbc:mysql://<HOST>:<PORT>/timo_news?sslMode=REQUIRED&serverTimezone=UTC&characterEncoding=UTF-8` |
| `DB_USER` | `avnadmin` |
| `DB_PASSWORD` | *(from Aiven)* |

### Free-tier behaviour

The free Render instance sleeps after a period of inactivity, so the first visit
afterwards cold-starts. Database connections are opened per request via `DriverManager`;
the DAOs close every connection, so this is adequate for demo traffic. A pooled
`DataSource` (HikariCP) would be the next step for sustained load.

---

## License

Released under the MIT License. See `LICENSE` if included.

---

*Originally built as coursework for JavaEE Enterprise Development Technology
(student ID 2442710013L).*
