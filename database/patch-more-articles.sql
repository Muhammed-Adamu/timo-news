-- Patch: Add more articles to categories that have few/no articles
-- Run this after init.sql to populate all categories
USE timo_news;

INSERT INTO news (title, slug, content, summary, image_url, category_id, author_id, status, view_count, is_featured, is_breaking) VALUES

-- World (category 8) — currently 0 articles
('United Nations Approves Global Digital Privacy Framework',
 'un-digital-privacy-framework-2026',
 '<p>The United Nations General Assembly voted overwhelmingly today to adopt a landmark Global Digital Privacy Framework, establishing baseline protections for personal data across 193 member states.</p><h2>Key Provisions</h2><p>The framework requires companies to obtain explicit consent before collecting personal data, mandates breach notification within 72 hours, and gives individuals the right to access and delete their data. Companies violating the rules face fines of up to 4% of global annual revenue.</p><p>The agreement comes after years of negotiations between privacy advocates and technology companies. Major tech firms including Google, Meta, and Apple have publicly supported the framework, calling it a step toward harmonized global standards.</p><h2>Implementation Timeline</h2><p>Member states have until 2028 to incorporate the framework into national law. A new UN Digital Privacy Office will be established in Geneva to monitor compliance and handle cross-border disputes.</p>',
 'The UN adopts a historic digital privacy treaty giving 193 nations baseline data protection standards and enforcement mechanisms.',
 'https://images.unsplash.com/photo-1563013544-824ae1b704d3?w=800', 8, 1, 'PUBLISHED', 6200, 0, 0),

('Massive Infrastructure Deal Signed Between Asian Economic Powers',
 'asian-infrastructure-deal-2026',
 '<p>China, Japan, South Korea, and ASEAN nations signed a $280 billion infrastructure development pact today in Singapore, marking the largest regional cooperation agreement in Asian history.</p><h2>Projects Planned</h2><p>The deal funds over 150 major projects including high-speed rail networks connecting Singapore to China, renewable energy farms across Southeast Asia, and next-generation port facilities. Construction is expected to create over 2 million jobs across the region over the next decade.</p><p>The agreement includes strict environmental protections and labor standards, addressing concerns raised by international observers during earlier negotiation rounds.</p><h2>Strategic Impact</h2><p>Analysts say the deal reshapes Asian geopolitics, creating economic interdependence that could reduce military tensions in the South China Sea and Korean Peninsula.</p>',
 'Asian nations sign a historic $280 billion infrastructure pact funding high-speed rail, clean energy, and ports across the continent.',
 'https://images.unsplash.com/photo-1524661135-423995f22d0b?w=800', 8, 1, 'PUBLISHED', 4100, 0, 0),

-- Politics (category 2) — currently 1 article, add 2 more
('Presidential Approval Ratings Hit New Highs After Economic Reforms',
 'presidential-approval-ratings-2026',
 '<p>Presidential approval ratings have surged to 62% according to the latest national poll, the highest level in over a decade, driven by strong public support for the administration\'s economic reform package.</p><h2>Driving Factors</h2><p>Key reforms include tax cuts for middle-income families, expanded healthcare subsidies, and a trillion-dollar infrastructure investment plan. Consumer confidence has risen 15 points since the reforms took effect, and unemployment has dropped to 3.4%, the lowest in 50 years.</p><p>The poll also shows strong bipartisan support for the infrastructure components, though healthcare reforms remain divisive along party lines.</p><h2>Midterm Outlook</h2><p>Political analysts suggest the approval numbers could help the incumbent party maintain control of Congress in the upcoming midterm elections, defying historical trends of midterm losses.</p>',
 'President approval ratings reach 62%, the highest in a decade, fueled by popular economic reforms and low unemployment.',
 'https://images.unsplash.com/photo-1529107386315-e1a2ed48a620?w=800', 2, 1, 'PUBLISHED', 8900, 0, 0),

('Landmark Voting Rights Bill Passes After Historic Senate Debate',
 'voting-rights-bill-2026',
 '<p>After a marathon 32-hour session — the longest in Senate history — the Voting Rights Protection Act passed with a 61-39 bipartisan majority. The bill represents the most significant expansion of voting access since the 1960s.</p><h2>What Changed</h2><p>The legislation mandates automatic voter registration for all eligible citizens, establishes Election Day as a federal holiday, requires at least 15 days of early voting in all states, and bans partisan gerrymandering through independent redistricting commissions.</p><p>It also modernizes voter registration systems with same-day registration and online portals, while requiring paper ballot backups for all electronic voting machines.</p><h2>Reactions</h2><p>Civil rights groups celebrated the passage as a victory for democracy. Some state officials have threatened legal challenges, arguing the bill infringes on state authority to administer elections.</p>',
 'The most sweeping voting rights legislation in 60 years passes the Senate, mandating automatic registration and ending gerrymandering.',
 'https://images.unsplash.com/photo-1540910419892-4a36d2c3266c?w=800', 2, 1, 'PUBLISHED', 7300, 0, 0),

-- Technology (category 1) — already has 2, add 1 more
('Quantum Computing Milestone: First Error-Corrected Logical Qubit Demonstrated',
 'quantum-computing-milestone-2026',
 '<p>Researchers at Google Quantum AI have demonstrated the world\'s first fully error-corrected logical qubit, a breakthrough that brings practical quantum computing significantly closer to reality.</p><h2>The Achievement</h2><p>Using a surface code on a 105-qubit processor, the team maintained a logical qubit for over 10 seconds with an error rate below the physical qubit threshold. This is the first time quantum error correction has been shown to actually improve qubit fidelity rather than just theoretically.</p><p>"This is the quantum equivalent of the Wright brothers\' first flight," said Dr. Hartmut Neven, head of Google Quantum AI. "We\'ve shown that error correction works in practice."</p><h2>Implications</h2><p>Practical quantum computers could revolutionize drug discovery, materials science, climate modeling, and cryptography. The breakthrough is expected to accelerate investment in the quantum computing sector, which is already projected to reach $65 billion by 2030.</p>',
 'Google achieves the first practical quantum error correction milestone, demonstrating a logical qubit with improved fidelity.',
 'https://images.unsplash.com/photo-1635070041078-e363dbe005cb?w=800', 1, 1, 'PUBLISHED', 13400, 1, 0),

-- Business (category 3) — already has 2, add 1 more
('Electric Vehicle Sales Overtake Combustion Engines in Europe for First Time',
 'ev-sales-overtake-ice-europe-2026',
 '<p>For the first time in history, electric vehicle sales surpassed internal combustion engine vehicles in Europe last quarter, marking a pivotal moment in the automotive industry\'s transition to sustainable transportation.</p><h2>By the Numbers</h2><p>EVs accounted for 51.2% of all new car registrations in the EU during Q2 2026, up from 38% a year earlier. Norway led with 94% EV market share, followed by the Netherlands at 72% and Germany at 58%.</p><p>The surge was driven by expanding charging infrastructure, falling battery costs, and new EU regulations requiring all new cars sold from 2035 to be zero-emission. Tesla, Volkswagen, and Chinese manufacturers BYD and NIO led sales.</p><h2>Industry Impact</h2><p>Traditional automakers are accelerating their EV transitions, with several announcing plans to phase out combustion engine production entirely by 2030.</p>',
 'Electric vehicles outsell combustion engine cars in Europe for the first time, accelerating the automotive transition.',
 'https://images.unsplash.com/photo-1593941707882-a5bba14938c7?w=800', 3, 1, 'PUBLISHED', 10200, 0, 0),

-- Sports (category 4) — already has 1, add 2 more
('Olympic Committee Announces Esports as Official Medal Event for 2032 Games',
 'olympics-esports-2032-2026',
 '<p>The International Olympic Committee made history today by announcing that competitive video gaming — esports — will be included as an official medal event at the 2032 Brisbane Olympic Games.</p><h2>Games and Format</h2><p>Three titles have been selected for the inaugural esports program: a racing simulation, a team strategy game, and a sports simulation title. Athletes will compete in dedicated arenas with live audiences, and events will be broadcast globally alongside traditional Olympic coverage.</p><p>National teams will qualify through regional tournaments organized by recognized esports federations, with strict anti-doping protocols and age restrictions in place.</p><h2>Reaction</h2><p>The decision has been met with both excitement from younger audiences and criticism from traditionalists. IOC President stated: "The Olympics must evolve to remain relevant. Esports requires extraordinary skill, strategy, and teamwork — all Olympic values."</p>',
 'The IOC announces esports will become an official Olympic medal sport at Brisbane 2032, with three game titles.',
 'https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800', 4, 1, 'PUBLISHED', 18700, 1, 0),

('Young Tennis Star Wins Fourth Consecutive Grand Slam Title',
 'tennis-star-fourth-slam-2026',
 '<p>At just 21 years old, tennis phenomenon Alina Kowalski claimed her fourth consecutive Grand Slam title at Wimbledon today, defeating the world number one in straight sets and cementing her place among the sport\'s all-time greats.</p><h2>Dominant Performance</h2><p>Kowalski\'s 6-2, 6-3, 6-4 victory showcased her trademark combination of power and precision. She served 12 aces and hit 38 winners against just 9 unforced errors — statistically one of the most dominant Grand Slam final performances ever recorded.</p><p>With this victory, she becomes only the fifth woman in history to hold all four Grand Slam titles simultaneously — a "Calendar Slam" — and the youngest ever to achieve the feat.</p><h2>What\'s Next</h2><p>Kowalski has already set her sights on breaking the all-time Grand Slam record of 24 titles. At her current pace, analysts project she could surpass the record by age 26.</p>',
 'A 21-year-old tennis prodigy wins her fourth straight Grand Slam, becoming the youngest player ever to hold all four majors.',
 'https://images.unsplash.com/photo-1595435934249-5df7ed86e1c0?w=800', 4, 1, 'PUBLISHED', 22300, 1, 0);

-- Add comments for new articles
INSERT INTO comments (news_id, user_id, content, status) VALUES
(11, 2, 'Finally! A global privacy standard is long overdue. This will change how tech companies operate worldwide.', 'APPROVED'),
(11, 3, 'Great news for consumers. The 72-hour breach notification requirement is especially important.', 'APPROVED'),
(12, 4, 'This infrastructure deal could transform Southeast Asia. The rail network alone will boost trade enormously.', 'APPROVED'),
(13, 2, 'Economic reforms are clearly working. The numbers speak for themselves.', 'APPROVED'),
(14, 3, 'Automatic voter registration should have been implemented decades ago. Better late than never!', 'APPROVED'),
(15, 2, 'Quantum computing is going to change everything. Imagine what this means for drug discovery!', 'APPROVED'),
(16, 4, 'The tipping point for EVs has finally arrived. Goodbye fossil fuels!', 'APPROVED'),
(17, 2, 'Esports in the Olympics? As a gamer, this is a dream come true!', 'APPROVED'),
(18, 3, 'Kowalski is truly generational talent. Four slams at 21 is unbelievable.', 'APPROVED');

-- Add likes for new articles
INSERT INTO likes (news_id, user_id) VALUES
(11, 2), (11, 3), (11, 4),
(12, 2), (12, 3),
(13, 2), (13, 3), (13, 4),
(14, 3), (14, 4),
(15, 2), (15, 3), (15, 4),
(16, 2), (16, 4),
(17, 2), (17, 3), (17, 4),
(18, 2), (18, 3), (18, 4);

SELECT 'Patch applied: Added articles for World, Politics, Technology, Business, Sports categories' AS result;
