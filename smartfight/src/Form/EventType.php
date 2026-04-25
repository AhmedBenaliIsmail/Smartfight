<?php

namespace App\Form;

use App\Entity\Event;
use App\Enum\EventStatus;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateTimeType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Vich\UploaderBundle\Form\Type\VichFileType;

class EventType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('name', TextType::class, [
                'label' => 'Event name',
                'attr' => [
                    'placeholder' => 'e.g. World Championship Boxing Night',
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
            ->add('startsAt', DateTimeType::class, [
                'label' => 'Starts at',
                'widget' => 'single_text',
                'required' => false,
            ])
            ->add('endsAt', DateTimeType::class, [
                'label' => 'Ends at',
                'widget' => 'single_text',
                'required' => false,
            ])
            ->add('status', ChoiceType::class, [
                'label' => 'Status',
                'choices' => array_combine(
                    array_map(fn(EventStatus $s) => $s->label(), EventStatus::cases()),
                    array_map(fn(EventStatus $s) => $s->value, EventStatus::cases()),
                ),
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
            ->add('venueName', TextType::class, [
                'label' => 'Venue name',
                'required' => false,
                'attr' => ['placeholder' => 'Arena or venue'],
            ])
            ->add('city', TextType::class, [
                'label' => 'City',
                'required' => false,
                'attr' => ['placeholder' => 'City'],
            ])
            ->add('country', TextType::class, [
                'label' => 'Country',
                'required' => false,
                'attr' => ['placeholder' => 'ISO code e.g. TN'],
            ])
            ->add('posterFile', VichFileType::class, [
                'label' => 'Event poster',
                'required' => false,
                'allow_delete' => true,
                'download_uri' => false,
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Event::class,
        ]);
    }
}
