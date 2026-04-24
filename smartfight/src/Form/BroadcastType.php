<?php

namespace App\Form;

use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\Validator\Constraints\Length;
use Symfony\Component\Validator\Constraints\NotBlank;

class BroadcastType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('audience', ChoiceType::class, [
                'choices' => [
                    'All Fans' => 'all',
                    'VIP Fans Only' => 'vip',
                    'Active This Month' => 'active',
                    'Prediction Players' => 'predictors',
                ],
                'expanded' => true,
                'constraints' => [new NotBlank()],
            ])
            ->add('type', ChoiceType::class, [
                'choices' => [
                    'Announcement' => 'ADMIN_BROADCAST',
                    'Event Update' => 'NEW_EVENT',
                    'Prediction Result' => 'PREDICTION_SCORED',
                ],
            ])
            ->add('title', TextType::class, [
                'constraints' => [new NotBlank(), new Length(['max' => 80])],
                'attr' => ['maxlength' => 80],
            ])
            ->add('message', TextareaType::class, [
                'constraints' => [new NotBlank(), new Length(['max' => 300])],
                'attr' => ['rows' => 4, 'maxlength' => 300],
            ]);
    }
}
