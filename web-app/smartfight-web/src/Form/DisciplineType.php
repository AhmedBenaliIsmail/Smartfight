<?php

namespace App\Form;

use App\Entity\Discipline;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints\Length;
use Symfony\Component\Validator\Constraints\NotBlank;

class DisciplineType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('name', TextType::class, [
                'label' => 'Discipline Name',
                'constraints' => [
                    new NotBlank(['message' => 'Name is required.']),
                    new Length([
                        'min' => 2, 'minMessage' => 'Minimum 2 characters.',
                        'max' => 100, 'maxMessage' => 'Maximum 100 characters.',
                    ]),
                ],
                'attr' => ['class' => 'form-control', 'placeholder' => 'Ex: MMA, Boxing, Judo...'],
            ])
            ->add('description', TextareaType::class, [
                'label' => 'Description',
                'required' => false,
                'attr' => ['class' => 'form-control', 'rows' => 4],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Discipline::class,
        ]);
    }
}
