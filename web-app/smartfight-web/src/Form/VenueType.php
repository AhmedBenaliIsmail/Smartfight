<?php

namespace App\Form;

use App\Entity\Venue;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints\Length;
use Symfony\Component\Validator\Constraints\NotBlank;
use Symfony\Component\Validator\Constraints\Positive;

class VenueType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('name', TextType::class, [
                'label' => 'Venue Name',
                'constraints' => [
                    new NotBlank(['message' => 'Name is required.']),
                    new Length(['min' => 2, 'minMessage' => 'Minimum 2 characters.']),
                ],
                'attr' => ['class' => 'form-control', 'placeholder' => 'Arena or stadium name'],
            ])
            ->add('city', TextType::class, [
                'label' => 'City',
                'constraints' => [
                    new NotBlank(['message' => 'City is required.']),
                ],
                'attr' => ['class' => 'form-control', 'placeholder' => 'City'],
            ])
            ->add('country', TextType::class, [
                'label' => 'Country',
                'constraints' => [
                    new NotBlank(['message' => 'Country is required.']),
                ],
                'attr' => ['class' => 'form-control', 'placeholder' => 'Country'],
            ])
            ->add('capacity', IntegerType::class, [
                'label' => 'Max Capacity',
                'required' => false,
                'constraints' => [
                    new Positive(['message' => 'Capacity must be positive.']),
                ],
                'attr' => ['class' => 'form-control'],
            ])
            ->add('address', TextType::class, [
                'label' => 'Address',
                'required' => false,
                'attr' => ['class' => 'form-control', 'placeholder' => 'Full address'],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Venue::class,
        ]);
    }
}
