# Product Requirements Document (PRD)

# Nooki

### MVP 1.0

**Tagline:** *Only the channels you choose.*

---

# 1. Product Overview

## Vision

Nooki מאפשר להורים ליצור לילדיהם חוויית צפייה בטוחה ופשוטה המבוססת על YouTube.

במקום לחשוף את הילדים לכל YouTube, Nooki מציג אך ורק תוכן מתוך רשימת ערוצים שההורה בחר ואישר.

המטרה היא לשלב את העושר של YouTube עם השליטה של ההורה.

---

# 2. Problem

הורים רוצים לאפשר לילדים לצפות ביוטיוב, אך אינם מעוניינים בחשיפה ל:

* המלצות אוטומטיות
* ערוצים לא מוכרים
* Shorts
* סרטונים קשורים
* חיפוש בלתי מוגבל

YouTube Kids אינו פותר את הבעיה כאשר ההורה רוצה לבחור בעצמו כל ערוץ, כולל ערוצים שאינם מוגדרים כערוצי ילדים.

---

# 3. Goal

לאפשר לילד לצפות אך ורק בתוכן מתוך רשימת ערוצים שאושרה מראש על ידי ההורה.

---

# 4. Target Platform

Android TV / Google TV

MVP מותאם במיוחד לסטרימרים כדוגמת Xiaomi TV Box.

---

# 5. Target Users

## Parent

אחראי על:

* יצירת PIN
* הוספת ערוצים
* מחיקת ערוצים

---

## Child

יכול:

* לצפות בסרטונים
* לחפש סרטונים
* להיכנס לערוצים
* לנגן סרטונים

אין לו הרשאות ניהול.

---

# 6. Product Principles

### PP-001

Only Approved Channels.

---

### PP-002

Child First Experience.

---

### PP-003

TV Remote Only.

---

### PP-004

Simple Before Smart.

---

### PP-005

No Backend.

---

# 7. User Flow

## First Launch

```text
Launch

↓

Create PIN

↓

Search First Channel

↓

Add Channel

↓

Home
```

לא ניתן להשתמש באפליקציה לפני שנוסף לפחות ערוץ אחד.

---

## Daily Flow

```אפליקציה:

עבור כל ערוץ מאושר:

* שלוף את 10 הסרטונים האחרונים לפי תאריך פרסום.
* מזג את כל הסרטונים.
* ערבב את הרשימה.
* אל תציג יותר משני סרטונים רצופים מאותו ערוץ.

---

### My Channels

מציג את כל הערוצים שאושרו.

---

# Feature 4

## Search

החיפוש מוגבל לערוצים המאושרים בלבד.

לעולם לא יוצגו תוצאות מערוצים אחרים.

---

# Feature 5

## Channel Page

מציג:

* שם הערוץ
* תמונת הערוץ
* רשימת הסרטונים האחרונים

---

# Feature 6

## Video Player

האפליקציה תשתמש בנגן הרשמי של YouTube.

בסיום סרטון יוצגו סרטונים נוספים מתוך הערוצים המאושרים בלבד.

---

# 9. Functional Requirements

### FR-001

Create PIN.

### FR-002

Validate PIN.

### FR-003

Search Channel.

### FR-004

Add Channel.

### FR-005

Remove Channel.

### FR-006

Display Recommended Feed.

### FR-007

Display Approved Channels.

### FR-008

Search Videos.

### FR-009

Open Channel.

### FR-010

Play Video.

### FR-011

Show Next Videos.

### FR-012

Persist Approved Channels locally.

---

# 10. Business Rules

### BR-001

כל סרטון חייב להשתייך לערוץ מאושר.

---

### BR-002

כל תוצאת חיפוש חייבת להשתייך לערוץ מאושר.

---

### BR-003

כל המלצה חייבת להשתייך לערוץ מאושר.

---

### BR-004

אין ניווט לתוכן מחוץ ל־Whitelist.

---

### BR-005

אין Shorts.

---

### BR-006

אין תגובות.

---

### BR-007

אין ערוצים קשורים.

---

### BR-008

אין פרופילים.

---

# 11. Local Storage

האפליקציה שומרת מקומית בלבד:

* PIN
* Approved Channels

לא נשמרים:

* Videos
* Feed
* Search Results
* Recommendations

---

# 12. Architecture

```text
Nooki

↓

Content Engine

↓

YouTube Data API

↓

Official YouTube Player
```

כל התוכן עובר דרך Content Engine.

אין גישה ישירה ל־YouTube מתוך מסכי האפליקציה.

---

# 13. Content Engine

### Responsibilities

* Build Home Feed
* Search Videos
* Load Channel Videos

### Feed Algorithm

```
for each approved channel

fetch latest 10 videos

merge all videos

shuffle

prevent more than two consecutive videos
from the same channel

return feed
```

---

# 14. Empty States

### No Channels

Message:

"No approved channels yet."

Action:

Add First Channel

---

### No Results

"No videos found."

---

### No Internet

"Internet connection required."

Action:

Retry

---

# 15. Security

* כל שינוי ברשימת הערוצים מחייב PIN.
* ילדים אינם יכולים להוסיף או להסיר ערוצים.
* אין אפשרות לנווט מתוך Nooki ל־YouTube הרגיל.

---

# 16. Performance

* Launch ≤ 3 seconds
* Screen transition ≤ 1 second
* Full Android TV remote support
* Optimised for 10-foot UI

---

# 17. Out of Scope

* Profiles
* Playlists
* Live
* Shorts
* History
* Continue Watching
* Favorites
* Categories
* AI Recommendations
* Backend
* Cloud Sync
* User Accounts
* Time Limits
* Device Management

---

# 18. MVP Definition of Done

ה־MVP ייחשב מוכן כאשר:

1. ניתן ליצור PIN.
2. ניתן להוסיף ערוץ באמצעות חיפוש.
3. ניתן להסיר ערוץ.
4. מסך הבית מציג Feed מתוך הערוצים המאושרים בלבד.
5. ניתן לחפש סרטונים רק בתוך הערוצים המאושרים.
6. ניתן לפתוח ערוץ.
7. ניתן לנגן סרטון.
8. לאחר סיום הסרטון מוצגים רק סרטונים מתוך הערוצים המאושרים.
9. אין אפשרות להגיע לתוכן שאינו שייך לרשימת הערוצים המאושרים.
10. כל האפליקציה ניתנת להפעלה באמצעות שלט Android TV בלבד.
