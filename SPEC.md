# SPEC.md: SupaVault (Supabase Android App with In-App Admin Panel)

## 1. Product Summary
**SupaVault** is a production-ready Android mobile application built using Kotlin and Jetpack Compose. It serves as a Cloud Media & Notes Vault designed to showcase and rigorously test:
1. **Supabase Auth:** User registration, login, logout, and token session persistence.
2. **Supabase Database (PostgreSQL):** Realtime/CRUD operations on user vault entries.
3. **Supabase Storage:** Direct media upload (photos/images) to Supabase Storage bucket (`vault_media`) and public URL retrieval.
4. **In-App Admin Panel:** Password-protected administrative console (`Admin@2026`) allowing administrators to monitor all user records, delete any content, and test bucket health.
5. **Dynamic Configuration:** In-app connection settings allowing users to inspect or update their `SUPABASE_URL` and `SUPABASE_ANON_KEY` dynamically.

## 2. User Roles & Permissions
- **Regular User:**
  - Can register and log in via Supabase Email/Password.
  - Can create vault notes with titles, descriptions, and media uploads.
  - Can view their own vault items in chronological order.
  - Can delete their own items.
- **Admin:**
  - Accessible via the top bar "Admin" button with master password **`Admin@2026`**.
  - Can view ALL items created by any user.
  - Can delete any offending item across the entire platform.
  - Can run storage upload diagnostic tests.

## 3. Data Model (PostgreSQL / Supabase)
Table: `vault_items`
- `id`: `uuid` PRIMARY KEY DEFAULT gen_random_uuid()
- `user_id`: `text` NOT NULL
- `user_email`: `text` NOT NULL
- `title`: `text` NOT NULL
- `description`: `text` NOT NULL
- `image_url`: `text` DEFAULT NULL
- `created_at`: `timestamptz` DEFAULT now()

Storage Bucket:
- Name: `vault_media` (Public bucket for images/media uploads)

## 4. Screens & Navigation
1. **AuthScreen:**
   - Sign In / Sign Up tabs.
   - Email and Password validation.
   - Quick Supabase Settings modal to configure URL and Anon Key.
2. **HomeScreen:**
   - TopAppBar with app title, Supabase Connection status indicator, Settings icon, Admin icon, and Logout button.
   - Floating Action Button (FAB) to add a new note/media.
   - LazyColumn feed of user notes with photo thumbnail, title, description, and delete button.
   - Empty state, loading state, error banner.
3. **CreateItemScreen / Modal:**
   - Input for Title and Description.
   - Image picker / sample media generator for instant upload testing.
   - Upload progress indicator.
   - Saves image to Supabase Storage `vault_media`, then inserts record into `vault_items`.
4. **AdminPanelScreen:**
   - Password prompt modal (`Admin@2026`).
   - Global dashboard showing total items and database status.
   - List of all user posts with user email labels and one-tap admin delete.
   - "Test Storage Upload" diagnostic button.

## 5. Security & Verification
- Admin passcode check: `Admin@2026` (configurable).
- Zero mock data: all calls hit Supabase endpoints.
- Error handling on network timeout or invalid credentials with clear toast/dialog messages.
