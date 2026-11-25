# SupaVault - Supabase Android Media & Notes App (with In-App Admin Panel)

A high-performance Android mobile application built with **Kotlin** and **Jetpack Compose** that fully integrates and tests **Supabase Auth**, **Supabase Database (PostgreSQL)**, **Supabase Storage**, and an **In-App Admin Panel**.

---

## 🚀 Key Features

1. **Supabase Authentication:**
   - Email & Password Sign Up and Sign In.
   - Session persistence and Secure Sign Out.
2. **Supabase Database (PostgreSQL):**
   - Real-time/CRUD operations on the `vault_items` table.
   - Personal user feed with title, descriptions, timestamps, and media references.
3. **Supabase Storage:**
   - Direct image file uploads to the `vault_media` public storage bucket.
   - Sample Asset Generator included in the app for testing storage uploads without searching for device images.
   - Public CDN URL resolution and image rendering with Coil.
4. **In-App Admin Panel:**
   - Admin unlock modal protected by master password: **`Admin@2026`**
   - Global dashboard showing total records, unique active users, and system status.
   - Admin delete: delete any post across any user.
   - Storage Health check: 1-tap live diagnostic upload test.
5. **Runtime Supabase Configuration:**
   - Switch or configure `SUPABASE_URL` and `SUPABASE_ANON_KEY` directly inside the app settings dialog.

---

## 🛠️ Supabase Backend Setup (1 Minute)

1. Open your [Supabase Dashboard](https://supabase.com/dashboard) and go to the **SQL Editor**.
2. Run the SQL script found in [`schema.sql`](schema.sql):
   - It creates the `vault_items` table with Row Level Security (RLS).
   - It creates the `vault_media` public storage bucket with access policies.
3. Copy your **Project URL** and **Anon Public Key** from `Project Settings > API`.
4. Enter them either directly inside the app on first launch or in `local.properties`.

---

## 🔑 Admin Credentials
- **Password:** `Admin@2026`
- **Access:** Tap the yellow shield icon in the top app bar on the Home screen.

---

## 📦 Automated GitHub Actions CI/CD
This repository includes a pre-configured GitHub Actions workflow in `.github/workflows/release.yml`.
When you push a git tag (e.g., `v1.0.0`), the workflow automatically builds the signed release APK and publishes a GitHub Release with the direct APK download link!
