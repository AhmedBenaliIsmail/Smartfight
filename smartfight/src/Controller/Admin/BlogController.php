<?php

namespace App\Controller\Admin;

use App\Entity\BlogArticle;
use App\Form\BlogArticleType;
use App\Repository\BlogArticleRepository;
use App\Repository\BlogCategoryRepository;
use App\Service\BlogService;
use Doctrine\ORM\EntityManagerInterface;
use Knp\Component\Pager\PaginatorInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

#[Route('/admin/blog', name: 'admin_blog_')]
class BlogController extends AbstractController
{
    #[Route('', name: 'index', methods: ['GET'])]
    public function index(
        Request $request,
        BlogArticleRepository $repo,
        BlogCategoryRepository $catRepo,
        PaginatorInterface $paginator,
    ): Response {
        $qb = $repo->findFilteredForAdmin(
            $request->query->get('search'),
            $request->query->getInt('category') ?: null,
            $request->query->get('status'),
        );

        $pagination = $paginator->paginate($qb, $request->query->getInt('page', 1), 10);

        return $this->render('admin/blog/index.html.twig', [
            'pagination' => $pagination,
            'categories' => $catRepo->findAll(),
            'filters' => $request->query->all(),
            'active_sidebar' => 'blog',
        ]);
    }

    #[Route('/new', name: 'new', methods: ['GET', 'POST'])]
    public function new(Request $request, EntityManagerInterface $em, BlogService $blogService): Response
    {
        $article = new BlogArticle();
        $form = $this->createForm(BlogArticleType::class, $article);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $imageFile = $form->get('imageFile')->getData();
            if ($imageFile) {
                $article->setImagePath($blogService->handleImageUpload($imageFile));
            }

            $em->persist($article);
            $em->flush();

            $this->addFlash('success', 'Article created successfully.');
            return $this->redirectToRoute('admin_blog_index');
        }

        return $this->render('admin/blog/form.html.twig', [
            'form' => $form->createView(),
            'article' => $article,
            'is_edit' => false,
            'active_sidebar' => 'blog',
        ]);
    }

    #[Route('/{id}', name: 'show', methods: ['GET'])]
    public function show(BlogArticle $article): Response
    {
        return $this->render('admin/blog/show.html.twig', [
            'article' => $article,
            'active_sidebar' => 'blog',
        ]);
    }

    #[Route('/{id}/edit', name: 'edit', methods: ['GET', 'POST'])]
    public function edit(Request $request, BlogArticle $article, EntityManagerInterface $em, BlogService $blogService): Response
    {
        $form = $this->createForm(BlogArticleType::class, $article);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $imageFile = $form->get('imageFile')->getData();
            if ($imageFile) {
                $article->setImagePath($blogService->handleImageUpload($imageFile));
            }

            $em->flush();

            $this->addFlash('success', 'Article updated successfully.');
            return $this->redirectToRoute('admin_blog_index');
        }

        return $this->render('admin/blog/form.html.twig', [
            'form' => $form->createView(),
            'article' => $article,
            'is_edit' => true,
            'active_sidebar' => 'blog',
        ]);
    }

    #[Route('/{id}/delete', name: 'delete', methods: ['POST'])]
    public function delete(Request $request, BlogArticle $article, EntityManagerInterface $em): Response
    {
        if ($this->isCsrfTokenValid('delete' . $article->getId(), $request->request->get('_token'))) {
            $em->remove($article);
            $em->flush();
            $this->addFlash('success', 'Article deleted.');
        }

        return $this->redirectToRoute('admin_blog_index');
    }
}
