<?php

namespace App\Form;

use App\Entity\BlogArticle;
use App\Entity\BlogCategory;
use App\Entity\User;
use Doctrine\ORM\EntityRepository;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints\File;
use Symfony\Component\Validator\Constraints\Length;
use Symfony\Component\Validator\Constraints\NotBlank;
use Vich\UploaderBundle\Form\Type\VichFileType;
use Vich\UploaderBundle\Form\Type\VichImageType;

class BlogArticleType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('title', TextType::class, [
                'constraints' => [new NotBlank(), new Length(['min' => 5, 'max' => 220])],
                'attr' => ['placeholder' => 'e.g. SmartFight Open 2026: Everything You Need to Know'],
            ])
            ->add('category', EntityType::class, [
                'class' => BlogCategory::class,
                'choice_label' => 'name',
                'placeholder' => 'Select category...',
                'constraints' => [new NotBlank()],
            ])
            ->add('author', EntityType::class, [
                'class' => User::class,
                'choice_label' => fn(User $u) => $u->getFirstName() . ' ' . $u->getLastName(),
                'placeholder' => 'Select author...',
                'constraints' => [new NotBlank()],
                'query_builder' => fn(EntityRepository $er) => $er->createQueryBuilder('u')
                    ->leftJoin('u.role', 'r')
                    ->where('r.name IN (:roles)')
                    ->setParameter('roles', ['ADMIN', 'FAN'])
                    ->orderBy('u.firstName', 'ASC'),
            ])
            ->add('content', TextareaType::class, [
                'constraints' => [new NotBlank()],
                'attr' => ['rows' => 10],
            ])
            ->add('summary', TextareaType::class, [
                'required' => false,
                'constraints' => [new Length(['max' => 500])],
                'attr' => ['rows' => 3, 'placeholder' => 'Short description shown in article listings'],
            ])
            ->add('status', ChoiceType::class, [
                'choices' => [
                    'Draft' => 'DRAFT',
                    'Published' => 'PUBLISHED',
                    'Archived' => 'ARCHIVED',
                ],
            ])
            ->add('imageFile', VichImageType::class, [
                'required' => false,
                'allow_delete' => true,
                'download_uri' => false,
                'image_uri' => true,
                'label' => 'Cover Image (JPEG / PNG / WEBP, max 5 MB)',
                'constraints' => [
                    new File([
                        'maxSize' => '5M',
                        'mimeTypes' => ['image/jpeg', 'image/png', 'image/webp'],
                        'mimeTypesMessage' => 'Please upload a valid image (JPEG, PNG, WEBP)',
                    ]),
                ],
            ])
            ->add('videoFile', VichFileType::class, [
                'required' => false,
                'allow_delete' => true,
                'download_uri' => false,
                'label' => 'Video (MP4 / WEBM, max 100 MB)',
                'constraints' => [
                    new File([
                        'maxSize' => '100M',
                        'mimeTypes' => ['video/mp4', 'video/webm'],
                        'mimeTypesMessage' => 'Please upload a valid video (MP4 or WEBM)',
                    ]),
                ],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => BlogArticle::class,
        ]);
    }
}
