# ChatBuddy - Anonymous Chatting App

## 🎯 Core Philosophy
1. **Build Core First:** Pahile basic chatting flow banvaycha.
2. **Deploy Early:** App chalayla lagla ki lagech AWS EC2 + Coolify var deploy karycha. Tyamule CI/CD ani DevOps chi practice hoil.
3. **Iterative Development:** Roz thode thode extra features add karat jayche.

## 🛠️ Technology Stack
- **Frontend:** Angular, Tailwind CSS, HTML/CSS (Manually Custom Design)
- **Backend:** Spring Boot (Java 21)
- **Database:** MongoDB
- **Real-time Communication:** WebSockets (Spring STOMP)
- **DevOps:** GitHub Actions, AWS EC2, Coolify

---

## 🚀 Application Flow & Roadmap

### Phase 1: Core Functionality & Deployment (MVP)
**Goal:** Ek user website var yeil ani bina login karta dusryashi chat karu shakel. He live deploy zala pahije.
- **Frontend:**
  - Ek attractive Landing Page jithun "Start Chatting" button asel.
  - Ek 'Global Chat Room' cha UI jithe messages disel ani send karta yetil.
- **Backend:**
  - Spring Boot madhe WebSockets setup karycha (real-time data sathi).
  - Messages handle karyche ani MongoDB madhe save karyche (fakt recent messages).
- **Deployment:**
  - AWS EC2 server setup karycha.
  - Coolify install karun he app live (production) karycha.

### Phase 2: User Engagement (Next Step)
**Goal:** Chatting cha experience azun interactive banavne.
- **Random Avatars & Aliases:** Jenvha koni join karel tenvha tyala swatahun ek random nav milel (eg. "Secret Ninja") ani avatar disel.
- **Private Rooms:** User ek navin room banvun chi link mitrala share karu shakto (fakt doghanchya private chat sathi).
- **Typing Status:** Koni type karat asel tar khali "Someone is typing..." asa disla pahije.
- **Online Counter:** App var kiti loka live ahet te disla pahije.

### Phase 3: Advanced Features (Roz navin add karyche)
**Goal:** App la modern features dene.
- **Self-Destructing Messages:** Snapchat sarkhe messages je 10 second nantr delete hotil.
- **Media Sharing:** Images ani GIFs send karychi facility.
- **Message Reactions:** Chat var emojis ne react karycha option (Like, Heart, etc).
- **Security & Reporting:** Jari app anonymous asel, tari koni ghal chat keli tar tyala block kiva report karycha feature.

---

## 🗄️ Database Design Idea (MongoDB)
Aplyala mukhya don Collections lagtil:
1. **Message:**
   - `id`
   - `content` (Chat text)
   - `senderAlias` (Random nav)
   - `roomId`
   - `timestamp`
2. **Room:**
   - `id`
   - `type` (GLOBAL or PRIVATE)
   - `createdAt`

*He document aapan project chya sobat thevu, mhanje reference sathi nehmi vachta yeil.*
