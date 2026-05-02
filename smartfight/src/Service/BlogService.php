<?php

namespace App\Service;

use Symfony\Component\String\Slugger\SluggerInterface;

class BlogService
{
    public function __construct(
        private SluggerInterface $slugger,
    ) {}

    public function generateSlug(string $title): string
    {
        return strtolower((string) $this->slugger->slug($title));
    }
}
