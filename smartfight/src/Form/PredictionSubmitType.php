<?php

namespace App\Form;

use App\Entity\FanPrediction;
use App\Entity\Fighter;
use App\Entity\MatchProposal;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints\NotBlank;

class PredictionSubmitType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('matchProposal', EntityType::class, [
                'class' => MatchProposal::class,
                'choice_label' => 'fightLabel',
                'placeholder' => 'Select fight...',
                'constraints' => [new NotBlank()],
            ])
            ->add('predictedWinner', EntityType::class, [
                'class' => Fighter::class,
                'choice_label' => 'displayName',
                'placeholder' => 'Select winner...',
                'constraints' => [new NotBlank()],
            ])
            ->add('predictedMethod', ChoiceType::class, [
                'choices' => [
                    'KO' => 'KO',
                    'TKO' => 'TKO',
                    'Submission' => 'SUBMISSION',
                    'Decision' => 'DECISION',
                    'Draw' => 'DRAW',
                ],
                'placeholder' => 'Select method...',
                'constraints' => [new NotBlank()],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => FanPrediction::class,
        ]);
    }
}
