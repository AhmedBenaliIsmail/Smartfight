<?php

namespace App\Entity;

use App\Repository\BlogArticleRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\HttpFoundation\File\File;
use Symfony\Component\Validator\Constraints as Assert;
use Vich\UploaderBundle\Mapping\Annotation as Vich;

#[Vich\Uploadable]
#[ORM\Entity(repositoryClass: BlogArticleRepository::class)]
#[ORM\Table(name: 'blog_article')]
#[ORM\HasLifecycleCallbacks]
class BlogArticle
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column(type: 'integer')]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: BlogCategory::class, inversedBy: 'articles')]
    #[ORM\JoinColumn(name: 'category_id', nullable: false)]
    #[Assert\NotBlank]
    private ?BlogCategory $category = null;

    #[ORM\ManyToOne(targetEntity: User::class)]
    #[ORM\JoinColumn(name: 'author_id', referencedColumnName: 'userId', nullable: false)]
    #[Assert\NotBlank]
    private ?User $author = null;

    #[ORM\Column(type: 'string', length: 220)]
    #[Assert\NotBlank]
    #[Assert\Length(min: 5, max: 220)]
    private string $title = '';

    #[ORM\Column(type: 'text')]
    #[Assert\NotBlank]
    private string $content = '';

    #[ORM\Column(type: 'text', nullable: true)]
    #[Assert\Length(max: 500)]
    private ?string $summary = null;

    #[ORM\Column(type: 'string', length: 20)]
    private string $status = 'DRAFT';

    #[ORM\Column(name: 'view_count', type: 'integer', options: ['default' => 0])]
    private int $viewCount = 0;

    #[ORM\Column(name: 'created_at', type: 'datetime')]
    private \DateTimeInterface $createdAt;

    #[ORM\Column(name: 'updated_at', type: 'datetime')]
    private \DateTimeInterface $updatedAt;

    #[ORM\Column(name: 'image_path', type: 'string', length: 255, nullable: true)]
    private ?string $imagePath = null;

    #[ORM\Column(name: 'video_path', type: 'string', length: 255, nullable: true)]
    private ?string $videoPath = null;

    #[Vich\UploadableField(mapping: 'blog_image', fileNameProperty: 'imagePath')]
    private ?File $imageFile = null;

    #[Vich\UploadableField(mapping: 'blog_video', fileNameProperty: 'videoPath')]
    private ?File $videoFile = null;

    #[ORM\PrePersist]
    public function onPrePersist(): void
    {
        $this->createdAt = new \DateTime();
        $this->updatedAt = new \DateTime();
    }

    #[ORM\PreUpdate]
    public function onPreUpdate(): void
    {
        $this->updatedAt = new \DateTime();
    }

    public function getId(): ?int { return $this->id; }
    public function getCategory(): ?BlogCategory { return $this->category; }
    public function setCategory(?BlogCategory $category): static { $this->category = $category; return $this; }
    public function getAuthor(): ?User { return $this->author; }
    public function setAuthor(?User $author): static { $this->author = $author; return $this; }
    public function getTitle(): string { return $this->title; }
    public function setTitle(string $title): static { $this->title = $title; return $this; }
    public function getContent(): string { return $this->content; }
    public function setContent(string $content): static { $this->content = $content; return $this; }
    public function getSummary(): ?string { return $this->summary; }
    public function setSummary(?string $summary): static { $this->summary = $summary; return $this; }
    public function getStatus(): string { return $this->status; }
    public function setStatus(string $status): static { $this->status = $status; return $this; }
    public function getViewCount(): int { return $this->viewCount; }
    public function getCreatedAt(): \DateTimeInterface { return $this->createdAt; }
    public function getUpdatedAt(): \DateTimeInterface { return $this->updatedAt; }
    public function getImagePath(): ?string { return $this->imagePath; }
    public function setImagePath(?string $imagePath): static { $this->imagePath = $imagePath; return $this; }
    public function getVideoPath(): ?string { return $this->videoPath; }
    public function setVideoPath(?string $videoPath): static { $this->videoPath = $videoPath; return $this; }

    public function setImageFile(?File $imageFile = null): static
    {
        $this->imageFile = $imageFile;
        if ($imageFile !== null) { $this->updatedAt = new \DateTime(); }
        return $this;
    }
    public function getImageFile(): ?File { return $this->imageFile; }

    public function setVideoFile(?File $videoFile = null): static
    {
        $this->videoFile = $videoFile;
        if ($videoFile !== null) { $this->updatedAt = new \DateTime(); }
        return $this;
    }
    public function getVideoFile(): ?File { return $this->videoFile; }
}
