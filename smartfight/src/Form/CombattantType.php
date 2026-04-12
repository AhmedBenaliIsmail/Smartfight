<?php
namespace App\Form;

use App\Entity\Combattant;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class CombattantType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('nickname', TextType::class, [
                'label' => false,
            ])
            ->add('nationalite', TextType::class, [
                'label' => false,
            ])
            ->add('weightClass', ChoiceType::class, [
                'label' => false,
                'choices' => [
                    'Flyweight (48-51 kg)'     => 'Flyweight',
                    'Featherweight (54-57 kg)' => 'Featherweight',
                    'Lightweight (57-63.5 kg)' => 'Lightweight',
                    'Welterweight (63.5-69 kg)'=> 'Welterweight',
                    'Middleweight (69-75 kg)'  => 'Middleweight',
                    'Heavyweight (91-120 kg)'  => 'Heavyweight',
                ],
                'placeholder' => '-- Choisir --',
            ])
            ->add('discipline', ChoiceType::class, [
                'label' => false,
                'choices' => [
                    'MMA'        => 'MMA',
                    'Boxing'     => 'Boxing',
                    'Kickboxing' => 'Kickboxing',
                    'Muay Thai'  => 'Muay Thai',
                    'BJJ'        => 'BJJ',
                    'Wrestling'  => 'Wrestling',
                ],
                'placeholder' => '-- Choisir --',
            ])
            ->add('wins', IntegerType::class, [
                'label' => false,
                'attr'  => ['min' => 0],
            ])
            ->add('losses', IntegerType::class, [
                'label' => false,
                'attr'  => ['min' => 0],
            ])
            ->add('draws', IntegerType::class, [
                'label' => false,
                'attr'  => ['min' => 0],
            ])
        ;
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Combattant::class,
        ]);
    }
}