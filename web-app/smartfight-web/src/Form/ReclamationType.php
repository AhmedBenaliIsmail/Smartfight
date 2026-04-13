<?php

namespace App\Form;

use App\Entity\Reclamation;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints\Length;
use Symfony\Component\Validator\Constraints\NotBlank;

class ReclamationType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('subject', TextType::class, [
                'label' => 'Subject',
                'constraints' => [
                    new NotBlank(['message' => 'Subject is required.']),
                    new Length([
                        'min' => 5, 'minMessage' => 'Minimum 5 characters.',
                        'max' => 200, 'maxMessage' => 'Maximum 200 characters.',
                    ]),
                ],
                'attr' => ['class' => 'form-control', 'placeholder' => 'Reclamation Subject'],
            ])
            ->add('description', TextareaType::class, [
                'label' => 'Detailed Description',
                'constraints' => [
                    new NotBlank(['message' => 'Description is required.']),
                    new Length([
                        'min' => 10, 'minMessage' => 'Minimum 10 characters.',
                        'max' => 2000,
                    ]),
                ],
                'attr' => ['class' => 'form-control', 'rows' => 6],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Reclamation::class,
        ]);
    }
}
