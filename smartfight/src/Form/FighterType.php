<?php

namespace App\Form;

use App\Entity\Fighter;
use App\Entity\User;
use Doctrine\ORM\EntityRepository;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Vich\UploaderBundle\Form\Type\VichImageType;

class FighterType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('user', EntityType::class, [
                'class' => User::class,
                'choice_label' => fn(User $u) => $u->getFirstName() . ' ' . $u->getLastName(),
                'placeholder' => 'Select user...',
                'query_builder' => fn(EntityRepository $er) => $er->createQueryBuilder('u')
                    ->orderBy('u.firstName', 'ASC'),
            ])
            ->add('nickname', TextType::class, [
                'required' => false,
                'attr' => ['placeholder' => 'Fighter nickname'],
            ])
            ->add('dateOfBirth', DateType::class, [
                'required' => false,
                'widget' => 'single_text',
            ])
            ->add('nationality', TextType::class, [
                'required' => false,
                'attr' => ['placeholder' => 'e.g. Tunisian'],
            ])
            ->add('weightClassId', IntegerType::class, [
                'required' => false,
                'label' => 'Weight Class ID',
            ])
            ->add('status', ChoiceType::class, [
                'choices' => [
                    'Active' => 'ACTIVE',
                    'Inactive' => 'INACTIVE',
                    'Suspended' => 'SUSPENDED',
                ],
            ])
            ->add('wins', IntegerType::class, ['attr' => ['min' => 0]])
            ->add('losses', IntegerType::class, ['attr' => ['min' => 0]])
            ->add('draws', IntegerType::class, ['attr' => ['min' => 0]])
            ->add('photoFile', VichImageType::class, [
                'required' => false,
                'allow_delete' => true,
                'download_uri' => false,
                'image_uri' => true,
                'label' => 'Fighter Photo (JPEG / PNG / WEBP)',
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Fighter::class,
        ]);
    }
}
