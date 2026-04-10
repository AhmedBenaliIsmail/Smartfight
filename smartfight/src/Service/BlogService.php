<?php

namespace App\Service;

use Symfony\Component\HttpFoundation\File\UploadedFile;
use Symfony\Component\String\Slugger\SluggerInterface;

class BlogService
{
    public function __construct(
        private string $uploadDir,
        private SluggerInterface $slugger,
    ) {}

    public function handleImageUpload(UploadedFile $file): string
    {
        $originalFilename = pathinfo($file->getClientOriginalName(), PATHINFO_FILENAME);
        $safeFilename = $this->slugger->slug($originalFilename);
        $newFilename = $safeFilename . '-' . uniqid() . '.' . $file->guessExtension();

        $file->move($this->uploadDir, $newFilename);

        return 'uploads/blog/' . $newFilename;
    }

    public function generateSlug(string $title): string
    {
        return strtolower((string) $this->slugger->slug($title));
    }
}
