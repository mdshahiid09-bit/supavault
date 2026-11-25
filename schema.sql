-- SupaVault Database and Storage Schema for Supabase
-- Copy and paste this script directly into Supabase SQL Editor and click RUN

-- 1. Create table for vault items
CREATE TABLE IF NOT EXISTS public.vault_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id TEXT NOT NULL,
    user_email TEXT NOT NULL,
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    image_url TEXT,
    created_at TIMESTAMPTZ DEFAULT timezone('utc'::text, now()) NOT NULL
);

-- 2. Enable Row Level Security (RLS)
ALTER TABLE public.vault_items ENABLE ROW LEVEL SECURITY;

-- 3. Policy for selecting items (Allow reading items)
CREATE POLICY "Allow select on vault_items" 
ON public.vault_items 
FOR SELECT 
USING (true);

-- 4. Policy for inserting items (Allow authenticated or anon insert)
CREATE POLICY "Allow insert on vault_items" 
ON public.vault_items 
FOR INSERT 
WITH CHECK (true);

-- 5. Policy for deleting items (Allow delete)
CREATE POLICY "Allow delete on vault_items" 
ON public.vault_items 
FOR DELETE 
USING (true);

-- 6. Storage Bucket setup
-- Ensure a public bucket named 'vault_media' exists
INSERT INTO storage.buckets (id, name, public)
VALUES ('vault_media', 'vault_media', true)
ON CONFLICT (id) DO NOTHING;

-- Storage policies for 'vault_media'
CREATE POLICY "Allow public read on vault_media"
ON storage.objects FOR SELECT
USING (bucket_id = 'vault_media');

CREATE POLICY "Allow public insert on vault_media"
ON storage.objects FOR INSERT
WITH CHECK (bucket_id = 'vault_media');

CREATE POLICY "Allow public delete on vault_media"
ON storage.objects FOR DELETE
USING (bucket_id = 'vault_media');
