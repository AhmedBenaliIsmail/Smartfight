<?php

namespace App\Form;

use App\Entity\Combat;
use App\Entity\Combattant;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class CombatType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('scoreIA')
            ->add('resultat')
            ->add('dateCombat')
            ->add('combattant1', EntityType::class, [
                'class' => Combattant::class,
                'choice_label' => 'id',
            ])
            ->add('combattant2', EntityType::class, [
                'class' => Combattant::class,
                'choice_label' => 'id',
            ])
        ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Combat::class,
        ]);
    }
}
