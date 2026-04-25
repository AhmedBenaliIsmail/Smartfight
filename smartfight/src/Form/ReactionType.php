<?php

namespace App\Form;

use App\Entity\FanReaction;
use App\Entity\FightResult;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints\Length;
use Symfony\Component\Validator\Constraints\NotBlank;

class ReactionType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('fightResult', EntityType::class, [
                'class' => FightResult::class,
                'choice_label' => fn(FightResult $fr) => $fr->getFightLabel(),
                'constraints' => [new NotBlank()],
            ])
            ->add('reactionType', ChoiceType::class, [
                'choices' => [
                    '🔥 Fire' => 'FIRE',
                    '😱 Shocked' => 'SHOCK',
                    '👏 Respect' => 'RESPECT',
                    '🏆 Dominant' => 'DOMINANT',
                    '💔 Controversial' => 'CONTROVERSIAL',
                ],
                'constraints' => [new NotBlank()],
            ])
            ->add('comment', TextareaType::class, [
                'required' => false,
                'constraints' => [new Length(['max' => 140])],
                'attr' => ['maxlength' => 140, 'placeholder' => 'Add a comment (optional)...'],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults(['data_class' => FanReaction::class]);
    }
}
