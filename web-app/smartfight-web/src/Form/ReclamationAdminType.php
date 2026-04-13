<?php

namespace App\Form;

use App\Entity\Reclamation;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints\NotBlank;

class ReclamationAdminType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('status', ChoiceType::class, [
                'label' => 'Status',
                'choices' => [
                    'Pending'       => 'PENDING',
                    'In Review'     => 'IN_REVIEW',
                    'Resolved'      => 'RESOLVED',
                    'Rejected'      => 'REJECTED',
                ],
                'constraints' => [new NotBlank(['message' => 'Status is required.'])],
                'attr' => ['class' => 'form-control'],
            ])
            ->add('adminResponse', TextareaType::class, [
                'label' => "Admin Response",
                'required' => false,
                'attr' => [
                    'class' => 'form-control',
                    'rows' => 5,
                    'placeholder' => 'Your response to the reclamation...',
                ],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Reclamation::class,
        ]);
    }
}
