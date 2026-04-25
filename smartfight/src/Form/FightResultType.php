<?php

namespace App\Form;

use App\Entity\Event;
use App\Entity\FightResult;
use App\Entity\Fighter;
use App\Enum\FightMethod;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TimeType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints\Choice;

class FightResultType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('event', EntityType::class, [
                'label' => 'Event',
                'class' => Event::class,
                'choice_label' => 'name',
            ])
            ->add('fightNumber', IntegerType::class, [
                'label' => 'Fight number',
                'attr' => [
                    'min' => 1,
                    'placeholder' => '1',
                ],
            ])
            ->add('fightDate', DateType::class, [
                'label' => 'Fight date',
                'widget' => 'single_text',
            ])
            ->add('fighterRed', EntityType::class, [
                'label' => 'Fighter red',
                'class' => Fighter::class,
                'choice_label' => 'displayName',
            ])
            ->add('fighterBlue', EntityType::class, [
                'label' => 'Fighter blue',
                'class' => Fighter::class,
                'choice_label' => 'displayName',
            ])
            ->add('winner', EntityType::class, [
                'label' => 'Winner',
                'class' => Fighter::class,
                'choice_label' => 'displayName',
                'required' => false,
                'placeholder' => 'Draw / undecided',
            ])
            ->add('status', ChoiceType::class, [
                'label' => 'Status',
                'choices' => [
                    'Scheduled' => 'SCHEDULED',
                    'Completed' => 'COMPLETED',
                    'Cancelled' => 'CANCELLED',
                ],
            ])
            ->add('method', ChoiceType::class, [
                'label' => 'Method',
                'choices' => array_flip(FightMethod::labels()),
                'constraints' => [
                    new Choice(['choices' => FightMethod::values()]),
                ],
            ])
            ->add('knockdownsFighterRed', IntegerType::class, [
                'label' => 'Knockdowns (Red)',
                'required' => false,
                'attr' => ['min' => 0, 'placeholder' => '0'],
            ])
            ->add('knockdownsFighterBlue', IntegerType::class, [
                'label' => 'Knockdowns (Blue)',
                'required' => false,
                'attr' => ['min' => 0, 'placeholder' => '0'],
            ])
            ->add('roundEnded', IntegerType::class, [
                'label' => 'Round ended',
                'required' => false,
                'attr' => [
                    'min' => 1,
                    'placeholder' => 'Optional',
                ],
            ])
            ->add('timeEnded', TimeType::class, [
                'label' => 'Time ended',
                'required' => false,
                'widget' => 'single_text',
                'input' => 'datetime',
            ])
            ->add('notes', TextareaType::class, [
                'label' => 'Notes',
                'required' => false,
                'attr' => [
                    'rows' => 3,
                    'placeholder' => 'Optional notes for officials or moderation.',
                ],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => FightResult::class,
        ]);
    }
}
