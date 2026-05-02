<?php

namespace App\Controller\Front;

use App\Entity\BlogArticle;
use App\Repository\BlogArticleRepository;
use App\Repository\BlogCategoryRepository;
use Knp\Component\Pager\PaginatorInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class BlogController extends AbstractController
{
    #[Route('/blog', name: 'front_blog_index', methods: ['GET'])]
    public function index(
        Request $request,
        BlogArticleRepository $repo,
        BlogCategoryRepository $catRepo,
        PaginatorInterface $paginator,
    ): Response {
        $qb = $repo->findPublishedQueryBuilder();

        $categorySlug = $request->query->get('category');
        if ($categorySlug) {
            $qb->join('a.category', 'cat')
               ->andWhere('cat.slug = :slug')
               ->setParameter('slug', $categorySlug);
        }

        $pagination = $paginator->paginate($qb, $request->query->getInt('page', 1), 9);
        $featured = $repo->findFeatured();

        return $this->render('front/blog/index.html.twig', [
            'pagination' => $pagination,
            'featured' => $featured,
            'categories' => $catRepo->findAllWithArticleCount(),
            'activeCategory' => $categorySlug,
        ]);
    }

    #[Route('/blog/{id}', name: 'front_blog_show', methods: ['GET'])]
    public function show(BlogArticle $article, BlogArticleRepository $repo, BlogCategoryRepository $catRepo): Response
    {
        $repo->incrementViewCount($article->getId());

        $related = $repo->findRelatedArticles(
            $article->getCategory()->getId(),
            $article->getId(),
        );

        return $this->render('front/blog/show.html.twig', [
            'article' => $article,
            'related' => $related,
            'categories' => $catRepo->findAllWithArticleCount(),
        ]);
    }
}
