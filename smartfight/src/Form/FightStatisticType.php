<?php

namespace App\Form;

use App\Entity\FightStatistic;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class FightStatisticType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('fightResultId', ChoiceType::class, [
                'label' => 'Fight result',
                'choices' => $options['fight_result_choices'],
            ])
            ->add('fighterId', ChoiceType::class, [
                'label' => 'Fighter',
                'choices' => $options['fighter_choices'],
            ])
            ->add('strikesLanded', IntegerType::class, [
                'label' => 'Strikes landed',
                'attr' => [
                    'min' => 0,
                    'placeholder' => '0',
                ],
            ])
            ->add('strikesAttempted', IntegerType::class, [
                'label' => 'Strikes attempted',
                'attr' => [
                    'min' => 0,
                    'placeholder' => '0',
                ],
            ])
            ->add('takedownsLanded', IntegerType::class, [
                'label' => 'Takedowns landed',
                'attr' => [
                    'min' => 0,
                    'placeholder' => '0',
                ],
            ])
            ->add('takedownsAttempted', IntegerType::class, [
                'label' => 'Takedowns attempted',
                'attr' => [
                    'min' => 0,
                    'placeholder' => '0',
                ],
            ])
            ->add('submissionAttempts', IntegerType::class, [
                'label' => 'Submission attempts',
                'attr' => [
                    'min' => 0,
                    'placeholder' => '0',
                ],
            ])
            ->add('knockdowns', IntegerType::class, [
                'label' => 'Knockdowns',
                'attr' => [
                    'min' => 0,
                    'placeholder' => '0',
                ],
            ])
            ->add('controlTimeSeconds', IntegerType::class, [
                'label' => 'Control time (seconds)',
                'attr' => [
                    'min' => 0,
                    'placeholder' => '0',
                ],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => FightStatistic::class,
            'fight_result_choices' => [],
            'fighter_choices' => [],
        ]);
    }
}
