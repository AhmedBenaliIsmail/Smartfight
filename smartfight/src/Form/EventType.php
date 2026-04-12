<?php

namespace App\Form;

use App\Entity\Discipline;
use App\Entity\Event;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\CheckboxType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;

class EventType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('name', TextType::class, [
                'label' => 'Event name',
                'attr' => [
                    'placeholder' => 'e.g. UFC Fight Night Paris',
                ],
            ])
            ->add('description', TextareaType::class, [
                'label' => 'Description',
                'required' => false,
                'attr' => [
                    'rows' => 3,
                    'placeholder' => 'Short summary for this event...',
                ],
            ])
            ->add('startDate', DateType::class, [
                'label' => 'Start date',
                'widget' => 'single_text',
            ])
            ->add('endDate', DateType::class, [
                'label' => 'End date',
                'widget' => 'single_text',
            ])
            ->add('status', ChoiceType::class, [
                'label' => 'Status',
                'choices' => [
                    'Scheduled' => 'SCHEDULED',
                    'Ongoing' => 'ONGOING',
                    'Completed' => 'COMPLETED',
                    'Cancelled' => 'CANCELLED',
                ],
            ])
            ->add('visibility', ChoiceType::class, [
                'label' => 'Visibility',
                'choices' => [
                    'Public' => 'PUBLIC',
                    'Private' => 'PRIVATE',
                ],
            ])
            ->add('capacity', IntegerType::class, [
                'label' => 'Capacity',
                'attr' => [
                    'min' => 0,
                    'placeholder' => '0',
                ],
            ])
            ->add('location', TextType::class, [
                'label' => 'Location',
                'required' => false,
                'attr' => [
                    'placeholder' => 'City, country',
                ],
            ])
            ->add('venueId', IntegerType::class, [
                'label' => 'Venue ID',
                'required' => false,
                'attr' => [
                    'placeholder' => 'Optional',
                ],
            ])
            ->add('organizerId', IntegerType::class, [
                'label' => 'Organizer ID',
                'required' => false,
                'attr' => [
                    'placeholder' => 'Optional',
                ],
            ])
            ->add('discipline', EntityType::class, [
                'label' => 'Discipline',
                'class' => Discipline::class,
                'choice_label' => 'name',
                'required' => false,
                'placeholder' => 'Select discipline',
            ])
            ->add('isChampionsEvent', CheckboxType::class, [
                'label' => 'Champions event',
                'required' => false,
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Event::class,
        ]);
    }
}
