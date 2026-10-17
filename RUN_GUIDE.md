# TimoNews — Setup & Run Guide

Recovery guide after the Windows reinstall. Written and verified on 2026-10-17.

---

## Project at a glance

| Item | Value |
|---|---|
| Type | Jakarta EE 9 web app (`.war`) — Servlets + JSP-less HTML/JS frontend |
| Language target | Java 11 (`maven.compiler.source/target`) |
| Servlet API | `jakarta.servlet` 5.0 — **requires Tomcat 10 or 11** |
| Database | MySQL 8, schema `timo_news` |
| Context path | `/timo-news` |
| URL | http://localhost:8080/timo-news/ |

> **Critical:** This app uses the `jakarta.*` namespace, not `javax.*`.
> **Tomcat 9 and older will NOT work** no matter what you configure. Use Tomcat 10.1.x.

---

## What was actually wrong

Three separate problems, all caused by the reformat:

1. **The database didn't exist.** MySQL 8 was running and the root password still matched
   the value in `DBUtil.java`, but the `timo_news` schema itself was gone — it lived in the
   old MySQL data directory. Every API call returned HTTP 500.

2. **IntelliJ's project SDK was broken.** `.idea/misc.xml` asks for a JDK named
   `openjdk-26 (2)`, but IntelliJ's JDK table only knows JDKs named `17` and `26` now, so
   nothing resolved and the whole project showed red errors.

3. **IntelliJ's Tomcat run config pointed at a Tomcat that isn't registered.** The saved run
   configuration references the app server `Tomcat 10.1.52`, but IntelliJ only has
   `Tomcat 10.1.59` registered. It reports the server as missing and refuses to run.

---

## Already fixed for you

- ✅ `timo_news` database created and seeded (`database/init.sql` + `database/patch-more-articles.sql`)
- ✅ `mvn clean package` verified — builds clean, `target/timo-news.war`
- ✅ WAR deployed and Tomcat started — **app is live right now at http://localhost:8080/timo-news/**
- ✅ Verified working: homepage, `/api/news`, `/api/categories`, `/api/search`, and admin login

Data loaded: 4 users · 8 categories · 18 articles · 17 comments · 36 likes

---

## Your environment (all still installed, correct versions)

| Tool | Location | Version |
|---|---|---|
| JDK 17 | `C:\Users\muham\dev\jdk-17` | 17.0.20 — `JAVA_HOME` points here ✅ |
| Maven | `C:\Users\muham\dev\maven` | 3.9.16 |
| Tomcat | `C:\Users\muham\dev\apache-tomcat-10.1.52` | 10.1.52 |
| Tomcat (alt) | `C:\apache-tomcat-10.1.59` | 10.1.59 — this is the one IntelliJ knows |
| MySQL | `C:\Program Files\MySQL\MySQL Server 8.0` | 8.0.46, service `MySQL80`, port 3306 |
| IntelliJ | `C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.1` | 2026.2.1 |

**Note on Java 26:** you have JDK 26 on `PATH` (`java -version` → 26), but `JAVA_HOME` points
to JDK 17. That's the correct setup — **leave `JAVA_HOME` on 17.** The project targets Java 11
and Tomcat 10.1 is happiest on a JDK 17/21.

---

## Fastest way to run it (command line — no IntelliJ needed)

Open **Git Bash** in the project folder.

### 1. Make sure MySQL is running

```bash
sc query MySQL80 | grep STATE      # want: STATE : 4 RUNNING
```

If it's stopped: `net start MySQL80` (may need an Administrator terminal).

### 2. Build

```bash
mvn clean package
```

### 3. Deploy + start Tomcat

```bash
cp target/timo-news.war /c/Users/muham/dev/apache-tomcat-10.1.52/webapps/

export JAVA_HOME="C:\Users\muham\dev\jdk-17"
export CATALINA_HOME="C:\Users\muham\dev\apache-tomcat-10.1.52"

cmd //c "C:\Users\muham\dev\apache-tomcat-10.1.52\bin\catalina.bat start"
```

> This command "hangs" — that's normal, it's the Tomcat launcher holding the console.
> Open a **new** terminal, or press `Ctrl+C` after you see the server started.

### 4. Open the app

http://localhost:8080/timo-news/

### 5. Stop Tomcat

```bash
cmd //c "C:\Users\muham\dev\apache-tomcat-10.1.52\bin\catalina.bat stop"
```

**Shortcut for later:** after a code change, re-run steps 2–3 and restart Tomcat.
Tomcat auto-deploys the WAR it finds in `webapps/`.

---

## Setting up IntelliJ (to fix the errors you saw)

### Step 1 — Fix the project SDK

1. **File → Project Structure** (`Ctrl+Alt+Shift+S`)
2. **Project** tab → **SDK** dropdown
3. Select **`17`** (not 26 — the project targets Java 11, and 17 is what Maven/`JAVA_HOME` use)
4. **Language level** → `11 - Local variable syntax for lambda parameters`
5. **OK**

### Step 2 — Register Tomcat

**Settings** (`Ctrl+Alt+S`) → **Build, Execution, Deployment → Application Servers** → **`+`**
→ **Tomcat Server** → set **Tomcat Home** to:

```
C:\Users\muham\dev\apache-tomcat-10.1.52
```

**OK**. (Alternatively point it at `C:\apache-tomcat-10.1.59`, already registered — either works,
both are Tomcat 10.1.x.)

### Step 3 — Fix the run configuration

The old one references `Tomcat 10.1.52`, which IntelliJ doesn't know about.

1. **Run → Edit Configurations…**
2. Select the **Tomcat 10.1.52** config on the left (it'll be flagged as invalid)
3. In **Server → Application server**, pick the Tomcat you registered in Step 2
4. **Deployment** tab → confirm the artifact is **`timo-news:war exploded`** → Application context: `/timo-news`
5. **OK**

> **If there's no run configuration at all** (or it's too broken to fix), delete it and make a
> new one: **Run → Edit Configurations → `+` → Tomcat Server → Local**, then on the
> **Deployment** tab click **`+` → Artifact → `timo-news:war exploded`**, and set the
> application context to `/timo-news`.

### Step 4 — Make sure the artifact exists

**File → Project Structure → Artifacts** — you should see **`timo-news:war exploded`**.
If not: **`+` → Web Application: Exploded → From Modules… → module `timo-news` → OK**.

### Step 5 — Load Maven and run

1. Right-click `pom.xml` → **Maven → Reload project** (or use the Maven tool window's refresh icon)
2. Wait for indexing to finish
3. Hit ▶️ **Run**

---

## Login credentials

Seed users from `database/init.sql`. **All four accounts use the same password:** `admin123`
(verified against the stored bcrypt hashes, not assumed).

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | **ADMIN** — full access to `/admin/*` |
| `john_reader` | `admin123` | USER |
| `jane_author` | `admin123` | USER |
| `mike_editor` | `admin123` | USER |

Admin pages: http://localhost:8080/timo-news/admin/dashboard.html

> If a login ever fails, run `database/patch-fix-passwords.sql` — it resets the hashes
> **without** dropping data.

---

## Troubleshooting

| Symptom | Cause & fix |
|---|---|
| `Address already in use: 8080` | Tomcat is already running. `catalina.bat stop`, or find it: `netstat -ano \| grep :8080` then `taskkill //PID <pid> //F` |
| HTTP 500 on every `/api/*` call | Database unreachable. Check `sc query MySQL80`, confirm `timo_news` exists: `mysql -u root -p -e "SHOW DATABASES;"` |
| `Unknown database 'timo_news'` | Re-run: `mysql -u root -p < database/init.sql` |
| `No database password configured` | Neither `db.properties` nor `DB_PASSWORD` supplied one. See **Database connection** above. |
| `ClassNotFoundException: jakarta.servlet.*` | Wrong Tomcat (9 or older). Must be Tomcat 10+. |
| Login returns `success:false` | Password hashes out of sync — run `database/patch-fix-passwords.sql` |
| IntelliJ: everything is red | Project SDK broken — do Step 1 above. |
| IntelliJ: "Application Server was not configured" | Do Steps 2–3 above. |
| Chinese text renders as `???` | Encoding. The `EncodingFilter` handles requests; also confirm Tomcat's `conf/server.xml` has `URIEncoding="UTF-8"` on the Connector. |
| `/admin/dashboard.html` returns 302 | **Not a bug.** `AuthenticationFilter` redirects logged-out users to the login page. Log in as `admin` first. |

### Verify the backend directly

```bash
curl http://localhost:8080/timo-news/api/categories
curl -X POST http://localhost:8080/timo-news/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

Both should return JSON with `"success":true`.

### Tomcat logs

```
C:\Users\muham\dev\apache-tomcat-10.1.52\logs\catalina.<date>.log
```

---

## Database connection

Credentials are **not** stored in the code. `DBUtil.java` resolves each setting in this
order (first non-blank value wins):

1. **Environment variables** — `DB_URL`, `DB_USER`, `DB_PASSWORD`
2. **`src/main/resources/db.properties`** (gitignored)
3. Built-in defaults for the URL and username (`localhost:3306/timo_news`, `root`).
   The password has **no default** — if neither source provides one, the connection fails
   with a message telling you both ways to set it.

### Local setup (already done on this machine)

`src/main/resources/db.properties` exists locally with:

```
db.url=jdbc:mysql://localhost:3306/timo_news?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
db.user=root
db.password=<your-mysql-password>
```

It is **gitignored** — verified with `git check-ignore`, it will never be committed.

### Setting it up on a new machine or fresh clone

```bash
cp src/main/resources/db.properties.example src/main/resources/db.properties
# then edit db.properties and fill in db.password
```

Or skip the file entirely and use environment variables:

```bash
export DB_PASSWORD='your-mysql-password'
```

> **Note:** `DB_PASSWORD` is currently also set as a **User** environment variable on this
> machine, at the same value as the file. Since env vars win, changing the password in
> `db.properties` alone will **not** take effect until you also update or remove that user
> variable:
> ```powershell
> [Environment]::GetEnvironmentVariable('DB_PASSWORD','User')
> ```

### Deployment

Env vars taking precedence is what makes deployment work — a PaaS (Render, Railway, etc.)
injects `DB_URL`, `DB_USER`, and `DB_PASSWORD` without any file needing to exist.

> **Caveat:** if `db.properties` exists when you run `mvn package`, it is bundled into
> `WEB-INF/classes/` inside the WAR. Delete it before a production build to avoid shipping
> your local password inside the artifact. The WAR itself is gitignored either way.

---

## Rebuilding the database from scratch

Only if you want a clean slate. **This wipes all data.**

```bash
mysql -u root -p < database/init.sql                     # schema + seed data
mysql -u root -p < database/patch-more-articles.sql      # extra articles for thin categories
```

---

## Deploying to Render + Aiven

The repo includes a `Dockerfile` and `render.yaml` for this setup.

### Why Docker is required

Render has **no native Java runtime**. A `.war` cannot be deployed directly — it runs as a
Docker web service. The Dockerfile:

1. Builds the WAR with Maven on JDK 17
2. Copies it into the official `tomcat:10.1-jdk17-temurin` image as **`ROOT.war`**, so the
   app is served at the domain root (`https://<service>.onrender.com/`) rather than under
   `/timo-news`. The frontend derives its context path from the script URL, so this works
   unchanged.
3. Rewrites Tomcat's connector from `8080` to Render's `$PORT` at container start.

### Aiven setup

1. Create an **Aiven for MySQL** service in the Aiven console.
2. Copy from the service's **Connection Information**:
   - Host, Port, User (`avnadmin`), Password, Database (usually `defaultdb`)
3. Load the schema into it:

   ```bash
   mysql --host <HOST> --port <PORT> --user avnadmin --password \
         --ssl-mode=REQUIRED < database/init.sql
   mysql --host <HOST> --port <PORT> --user avnadmin --password \
         --ssl-mode=REQUIRED < database/patch-more-articles.sql
   ```

   `init.sql` creates a `timo_news` database. Aiven's `avnadmin` normally has rights to do
   that; if your plan restricts it, edit the script's `CREATE DATABASE` / `USE` lines to
   `defaultdb` and point `DB_URL` there instead.

### Render setup

1. Push this repo to GitHub.
2. In Render: **New → Blueprint**, select the repo. `render.yaml` is detected automatically.
3. Render prompts for the three `sync: false` values. Use:

   | Variable | Value |
   |---|---|
   | `DB_URL` | `jdbc:mysql://<HOST>:<PORT>/timo_news?sslMode=REQUIRED&serverTimezone=UTC&characterEncoding=UTF-8` |
   | `DB_USER` | `avnadmin` |
   | `DB_PASSWORD` | *(from Aiven)* |

   **`sslMode=REQUIRED` is mandatory** — Aiven rejects unencrypted connections.

4. Deploy. First build takes a few minutes (Maven downloads dependencies).

### After deploying

```bash
curl https://<service>.onrender.com/api/categories
```

If that returns JSON, the app and database are connected.

### Known limitations

- **Free tier spins down** after inactivity — the first visit afterwards cold-starts.
- **No connection pooling.** `DBUtil` opens a connection per request via `DriverManager`.
  Under load this can hit Aiven's connection limit. Swapping in HikariCP is the fix.
- **`db.properties` is excluded from the image** via `.dockerignore`, so credentials come
  only from Render's environment variables.

---

## Next steps (not done yet)

1. ✅ **Credentials externalized** — no password remains in any tracked source file.
2. ✅ **Deployment files added** — `Dockerfile`, `.dockerignore`, `render.yaml`, `docker/entrypoint.sh`.
3. **Push to Git** — the project isn't a repo yet (`git init` still needed), and there's no
   `README.md` yet.
4. **Deploy** — follow the Render + Aiven steps above.
5. **Optional hardening** — replace `DriverManager` with HikariCP before real traffic.
