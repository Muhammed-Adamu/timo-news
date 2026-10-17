-- ============================================================
-- TimoNews - Comprehensive News Website
-- Database Initialization Script
-- ============================================================

CREATE DATABASE IF NOT EXISTS timo_news
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE timo_news;

-- ============================================================
-- Table: users
-- ============================================================
DROP TABLE IF EXISTS likes;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS news;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS contacts;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    avatar VARCHAR(255) DEFAULT 'default-avatar.png',
    role ENUM('USER', 'ADMIN') DEFAULT 'USER',
    bio TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================
-- Table: categories
-- ============================================================
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    slug VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    icon_class VARCHAR(50) DEFAULT 'bi-folder',
    color VARCHAR(7) DEFAULT '#6c757d',
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================
-- Table: news
-- ============================================================
CREATE TABLE news (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    content LONGTEXT NOT NULL,
    summary VARCHAR(500),
    image_url VARCHAR(255) DEFAULT 'default-news.jpg',
    category_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    status ENUM('DRAFT', 'PUBLISHED') DEFAULT 'PUBLISHED',
    view_count BIGINT DEFAULT 0,
    is_featured TINYINT(1) DEFAULT 0,
    is_breaking TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_category (category_id),
    INDEX idx_author (author_id),
    INDEX idx_status (status),
    INDEX idx_featured (is_featured),
    INDEX idx_breaking (is_breaking),
    INDEX idx_views (view_count),
    INDEX idx_created (created_at)
) ENGINE=InnoDB;

-- ============================================================
-- Table: comments
-- ============================================================
CREATE TABLE comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    news_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    status ENUM('APPROVED', 'PENDING') DEFAULT 'APPROVED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (news_id) REFERENCES news(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_news (news_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB;

-- ============================================================
-- Table: likes
-- ============================================================
CREATE TABLE likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    news_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (news_id) REFERENCES news(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_news_user (news_id, user_id),
    INDEX idx_news (news_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB;

-- ============================================================
-- Table: contacts
-- ============================================================
CREATE TABLE contacts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL,
    subject VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    is_read TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ============================================================
-- Seed Data: Admin User (password: admin123)
-- ============================================================
INSERT INTO users (username, password, email, full_name, role, bio) VALUES
('admin', '$2b$12$lWSVxEjwrnoqzJESaaw1o.raSOmEHHElIYTOwDz7lHY3VxM5PX.qW',
 'admin@timonews.com', 'Site Administrator', 'ADMIN', 'TimoNews platform administrator'),
('john_reader', '$2b$12$KI2RMkTxJnTG2DjnAFi1y.3WiSScK9SF5w.X/sVZ5Ut/tQyGt5YMO',
 'john@example.com', 'John Doe', 'USER', 'Avid news reader and tech enthusiast'),
('jane_author', '$2b$12$cAIOEizaDWlkYU6zr08WXeFkm031VZ8iE81yV2V0DXfUUIw5MU5hC',
 'jane@example.com', 'Jane Smith', 'USER', 'Freelance journalist and blogger'),
('mike_editor', '$2b$12$ozQ4iuSQZCZcfThGsDNsAO42LDFxaidGzIiVQHS8i8tpzwdgS0XQK',
 'mike@example.com', 'Mike Johnson', 'USER', 'Editor at large, covering global events');

-- ============================================================
-- Seed Data: Categories
-- ============================================================
INSERT INTO categories (name, slug, description, icon_class, color, sort_order) VALUES
('Technology', 'technology', 'Latest tech news, gadgets, AI, and innovations', 'bi-cpu', '#3b82f6', 1),
('Politics', 'politics', 'Political news, policies, and government updates', 'bi-building', '#ef4444', 2),
('Business', 'business', 'Business news, markets, finance, and economy', 'bi-briefcase', '#10b981', 3),
('Sports', 'sports', 'Sports coverage, matches, and athlete profiles', 'bi-trophy', '#f59e0b', 4),
('Entertainment', 'entertainment', 'Movies, music, celebrities, and culture', 'bi-film', '#8b5cf6', 5),
('Health', 'health', 'Health news, medical research, and wellness tips', 'bi-heart-pulse', '#ec4899', 6),
('Science', 'science', 'Scientific discoveries, research, and space exploration', 'bi-rocket-takeoff', '#06b6d4', 7),
('World', 'world', 'International news and global affairs', 'bi-globe2', '#6366f1', 8);

-- ============================================================
-- Seed Data: News Articles
-- ============================================================
INSERT INTO news (title, slug, content, summary, image_url, category_id, author_id,
                  status, view_count, is_featured, is_breaking) VALUES

('The Rise of Artificial Intelligence in Modern Healthcare',
 'rise-of-ai-healthcare-2026',
 '<p>Artificial Intelligence is revolutionizing the healthcare industry in unprecedented ways. From diagnostic tools that can detect diseases earlier than human doctors, to personalized treatment plans based on genetic analysis, AI is changing how we approach medicine.</p><h2>Key Developments</h2><p>In 2026, AI-powered diagnostic systems achieved a 99.7% accuracy rate in detecting early-stage lung cancer from CT scans, surpassing the 96% rate of experienced radiologists. Machine learning algorithms are now being used to predict patient deterioration up to 48 hours before it occurs, allowing medical staff to intervene proactively.</p><p>Wearable devices equipped with AI can now monitor vital signs continuously and alert users to potential health issues before symptoms appear. Companies like Apple and Google have invested billions in health AI research, with the global AI healthcare market projected to reach $188 billion by 2030.</p><h2>Challenges Ahead</h2><p>Despite the promise, challenges remain around data privacy, algorithmic bias, and regulatory approval. Critics argue that AI should supplement, not replace, human medical judgment.</p>',
 'AI is transforming healthcare with breakthrough diagnostic tools, personalized treatments, and predictive analytics that promise to reshape the medical landscape.',
 'https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?w=800',
 7, 1, 'PUBLISHED', 15420, 1, 0),

('Global Markets Surge as Trade Agreement Reached',
 'global-markets-trade-agreement-2026',
 '<p>Stock markets around the world rallied today following the announcement of a landmark trade agreement between major economies. The deal, which has been under negotiation for over two years, aims to reduce tariffs and streamline cross-border commerce.</p><h2>Market Reaction</h2><p>The S&P 500 jumped 3.2% in morning trading, while the FTSE 100 gained 2.8%. Asian markets led the rally, with the Nikkei 225 climbing 4.1% and the Shanghai Composite rising 3.5%. Technology and manufacturing stocks saw the biggest gains.</p><p>Currency markets also reacted strongly, with the US dollar strengthening against most major currencies. Analysts predict sustained growth in international trade volumes through the remainder of 2026.</p><h2>What This Means</h2><p>For businesses, the agreement means reduced costs and faster customs processing. For consumers, it could translate to lower prices on imported goods. However, some domestic industries have expressed concerns about increased competition.</p>',
 'Stock markets worldwide surge following a landmark international trade agreement that promises to reshape global commerce.',
 'https://images.unsplash.com/photo-1611974789855-9c2a0a7236a3?w=800',
 3, 1, 'PUBLISHED', 12350, 1, 1),

('Championship Finals: Underdogs Make Historic Comeback',
 'championship-finals-underdog-comeback-2026',
 '<p>In one of the most thrilling championship finals in recent memory, the underdog team staged an extraordinary comeback to claim the title. Trailing by 18 points at halftime, they rallied in the second half to force overtime, eventually winning 112-108.</p><h2>The Turning Point</h2><p>The comeback was led by rookie sensation Marcus Chen, who scored 28 of his 42 points in the second half and overtime. His performance has drawn comparisons to some of the greatest finals performances in history. "I just believed in my teammates," Chen said after the game.</p><p>The victory marks the first championship for the franchise in 34 years, sparking celebrations across the city. Fans poured into the streets, with estimates suggesting over 500,000 people participated in the impromptu parade.</p><h2>Looking Forward</h2><p>With a young core of talented players, analysts predict this could be the beginning of a dynasty. The off-season will be crucial for retaining key free agents and building on this momentum.</p>',
 'An incredible second-half rally leads to one of the greatest championship victories in sports history.',
 'https://images.unsplash.com/photo-1461749280684-dccba630e2f6?w=800',
 4, 1, 'PUBLISHED', 18920, 1, 0),

('SpaceX Successfully Launches Mars Colony Supply Mission',
 'spacex-mars-colony-supply-2026',
 '<p>SpaceX achieved another milestone today with the successful launch of its largest-ever Mars colony supply mission. The Starship Heavy rocket carried 100 tons of supplies, including habitat modules, food supplies, and scientific equipment destined for the growing Mars colony.</p><h2>Mission Details</h2><p>The launch took place at 6:42 AM EST from the Kennedy Space Center. The first-stage booster successfully landed on the drone ship in the Atlantic Ocean, marking the 200th successful booster recovery for SpaceX. The payload includes advanced 3D printers capable of manufacturing tools and spare parts using Martian soil.</p><p>This mission supports the 50-person research outpost established on Mars in 2025. The colony has been operating for 18 months, conducting experiments in agriculture, geology, and atmospheric science.</p><h2>Future Plans</h2><p>Elon Musk confirmed that SpaceX plans to send the first civilian passengers to Mars by 2028, with ticket prices expected to be around $500,000 per person — down from the initial $2 million estimate in 2024.</p>',
 'SpaceX launches its largest Mars supply mission yet, carrying 100 tons of cargo to support the growing Martian research colony.',
 'https://images.unsplash.com/photo-1517976487492-5750f3195933?w=800',
 7, 1, 'PUBLISHED', 22100, 1, 0),

('New Study Reveals Mediterranean Diet Reduces Heart Disease Risk by 30%',
 'mediterranean-diet-heart-health-2026',
 '<p>A comprehensive 15-year study published in the New England Journal of Medicine has found that adhering to a Mediterranean diet can reduce the risk of cardiovascular disease by up to 30%. The study tracked over 25,000 participants across seven countries.</p><h2>Key Findings</h2><p>Researchers found that the diet\'s combination of olive oil, nuts, fresh vegetables, and moderate fish consumption provides powerful anti-inflammatory effects. Participants who strictly followed the diet showed significantly lower levels of LDL cholesterol and improved arterial health.</p><p>The study also revealed cognitive benefits, with adherents showing a 22% lower risk of developing Alzheimer\'s disease. "This is the strongest evidence yet that diet plays a crucial role in long-term health outcomes," said Dr. Elena Rodriguez, lead researcher.</p><h2>Practical Advice</h2><p>Health experts recommend incorporating key elements of the Mediterranean diet gradually: replace butter with olive oil, increase vegetable intake, and eat fish twice a week.</p>',
 'A landmark 15-year study confirms the Mediterranean diet dramatically reduces heart disease risk and may protect cognitive function.',
 'https://images.unsplash.com/photo-1498837167922-ddd27525d352?w=800',
 6, 1, 'PUBLISHED', 8750, 1, 0),

('Tech Giants Announce Joint AI Safety Initiative',
 'tech-giants-ai-safety-initiative-2026',
 '<p>In an unprecedented move, the world\'s five largest technology companies — Google, Microsoft, Apple, Amazon, and Meta — announced a joint AI Safety Initiative aimed at establishing industry-wide standards for artificial intelligence development.</p><h2>The Framework</h2><p>The initiative, called the "AI Safety Pledge," includes commitments to third-party auditing of AI systems, transparency in training data sources, and a shared "kill switch" protocol for potentially dangerous AI systems. An independent oversight board, composed of ethicists, engineers, and policy experts, will be established to enforce compliance.</p><p>The companies have collectively pledged $10 billion over five years for AI safety research. This includes funding for university research programs and the establishment of a dedicated AI Safety Research Center in Geneva.</p><h2>Industry Impact</h2><p>Smaller AI startups have expressed mixed reactions. Some welcome the standards as providing clear guidelines, while others worry the requirements could stifle innovation and create barriers to entry.</p>',
 'Five major tech companies unite to create comprehensive AI safety standards and pledge $10 billion for research.',
 'https://images.unsplash.com/photo-1677442136019-21780ecad995?w=800',
 1, 1, 'PUBLISHED', 11200, 0, 1),

('Historic Climate Agreement Signed by 195 Nations',
 'historic-climate-agreement-2026',
 '<p>Representatives from 195 nations signed a historic climate agreement today in Geneva, committing to legally binding carbon reduction targets. The agreement, known as the Geneva Climate Accord, sets the most ambitious environmental goals in history.</p><h2>Key Targets</h2><p>The accord mandates a 65% reduction in carbon emissions by 2035, compared to 2005 levels. It also establishes a $500 billion fund to help developing nations transition to clean energy. The fund will be financed by a combination of government contributions and a new global carbon tax on heavy industries.</p><p>For the first time, the agreement includes enforcement mechanisms. Nations that fail to meet their targets face trade sanctions and reduced access to the climate fund. An independent monitoring body will track emissions using satellite technology and on-the-ground inspections.</p><h2>Global Reaction</h2><p>Environmental groups have cautiously welcomed the agreement, praising its ambition while expressing concern about implementation. Business leaders have called for clarity on how the new regulations will affect international trade.</p>',
 'A landmark climate accord signed by 195 nations sets unprecedented carbon reduction targets with enforcement mechanisms.',
 'https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=800',
 2, 1, 'PUBLISHED', 9650, 0, 0),

('Blockchain Revolution: Central Banks Launch Digital Currencies',
 'central-bank-digital-currencies-2026',
 '<p>In a coordinated move, 45 central banks simultaneously launched their digital currencies (CBDCs) this week, marking the largest transformation of the global financial system since the Bretton Woods agreement of 1944.</p><h2>How It Works</h2><p>Unlike cryptocurrencies like Bitcoin, these CBDCs are fully backed by sovereign governments and carry the same legal tender status as physical cash. Transactions are processed on permissioned blockchain networks that can handle over 100,000 transactions per second.</p><p>The digital currencies integrate with existing banking infrastructure while enabling instant, near-zero-cost cross-border payments. Early data shows international settlement times dropping from 3-5 days to under 10 seconds.</p><h2>Privacy and Security</h2><p>Regulators emphasize that the CBDCs include privacy protections, with transaction data accessible only to the account holder and their chosen financial institution — not the government. However, privacy advocates remain skeptical, calling for stronger safeguards.</p>',
 'Forty-five central banks debut sovereign digital currencies built on blockchain technology, transforming global finance.',
 'https://images.unsplash.com/photo-1639762681057-408e52192e55?w=800',
 3, 1, 'PUBLISHED', 7800, 0, 0),

('Record-Breaking Box Office: Summer Blockbuster Crosses $3 Billion',
 'summer-blockbuster-3-billion-2026',
 '<p>The summer\'s most anticipated film "Stellar Horizon" has shattered box office records, becoming the first movie in history to gross over $3 billion globally. The sci-fi epic, directed by acclaimed filmmaker Ava Chen, has captivated audiences worldwide.</p><h2>Breaking Records</h2><p>In its opening weekend alone, "Stellar Horizon" earned $850 million globally, breaking the previous record by over $200 million. The film has maintained strong momentum, staying at number one for eight consecutive weeks in most markets.</p><p>Critics have praised the film\'s groundbreaking visual effects and emotionally resonant storyline. The movie was shot using a new proprietary camera system that captures images in 16K resolution, creating an unprecedented level of visual immersion.</p><h2>Cultural Impact</h2><p>Beyond its financial success, the film has sparked widespread discussions about space exploration and humanity\'s future. NASA reported a 300% increase in job applications following the film\'s release.</p>',
 '"Stellar Horizon" becomes the first film to surpass $3 billion at the global box office, redefining cinematic achievement.',
 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=800',
 5, 1, 'PUBLISHED', 24500, 1, 0),

('Revolutionary Battery Technology Promises 1000-Mile Electric Vehicles',
 '1000-mile-ev-battery-breakthrough-2026',
 '<p>Researchers at MIT have announced a breakthrough in solid-state battery technology that could enable electric vehicles to travel over 1,000 miles on a single charge. The new batteries charge in under 15 minutes and last for over 1 million miles.</p><h2>The Technology</h2><p>The breakthrough involves a new lithium-sulfur solid electrolyte that overcomes the dendrite formation problem that has plagued solid-state batteries for decades. The batteries use abundant, cheap materials, potentially making them cheaper than current lithium-ion batteries at scale.</p><p>Several major automakers, including Tesla, Toyota, and Volkswagen, have already licensed the technology. Mass production is expected to begin in 2028, with the first vehicles equipped with these batteries hitting the market in 2029.</p><h2>Environmental Impact</h2><p>If widely adopted, the technology could accelerate the transition away from fossil fuel vehicles, potentially reducing global carbon emissions by up to 15%. The batteries are also fully recyclable, addressing concerns about battery waste.</p>',
 'MIT researchers develop solid-state batteries enabling 1,000-mile EV range with 15-minute charging, set to transform transportation.',
 'https://images.unsplash.com/photo-1518770660439-4636190af475?w=800',
 1, 1, 'PUBLISHED', 15600, 1, 0);

-- ============================================================
-- Seed Data: Comments
-- ============================================================
INSERT INTO comments (news_id, user_id, content, status) VALUES
(1, 2, 'This is fascinating! AI in healthcare could save so many lives. Great to see the technology advancing so rapidly.', 'APPROVED'),
(1, 3, 'I work in healthcare and we are already seeing the benefits of AI diagnostic tools. The accuracy is truly impressive.', 'APPROVED'),
(1, 4, 'Important to remember that AI should augment doctors, not replace them. The human touch in medicine is irreplaceable.', 'APPROVED'),
(2, 2, 'Finally some good news for the global economy! Hopefully this translates to more jobs and stability.', 'APPROVED'),
(3, 3, 'What a game! Marcus Chen is going to be a legend. That fourth quarter performance was unbelievable!', 'APPROVED'),
(4, 2, 'Mars colonization is getting closer every year. Cant wait to see the first civilian missions!', 'APPROVED'),
(5, 4, 'I switched to Mediterranean diet two years ago and my cholesterol dropped significantly. This study confirms what many of us have experienced.', 'APPROVED'),
(10, 3, '1,000 mile range would completely eliminate range anxiety. This is the breakthrough EV adoption needed!', 'APPROVED');

-- ============================================================
-- Seed Data: Likes
-- ============================================================
INSERT INTO likes (news_id, user_id) VALUES
(1, 2), (1, 3), (1, 4),
(2, 2), (2, 3),
(3, 2), (3, 3), (3, 4),
(4, 2), (4, 3),
(5, 2), (5, 4),
(10, 2), (10, 3), (10, 4);
