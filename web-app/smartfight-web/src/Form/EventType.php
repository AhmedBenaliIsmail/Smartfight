<?php

namespace App\Form;

use App\Entity\Discipline;
use App\Entity\Event;
use App\Entity\Venue;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\DateType;
use Symfony\Component\Form\Extension\Core\Type\IntegerType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\Form\FormError;
use Symfony\Component\Form\FormEvent;
use Symfony\Component\Form\FormEvents;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints\Length;
use Symfony\Component\Validator\Constraints\NotBlank;
use Symfony\Component\Validator\Constraints\NotNull;
use Symfony\Component\Validator\Constraints\Positive;
use Symfony\Component\Validator\Constraints\Range;

class EventType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('name', TextType::class, [
                'label' => "Event Name",
                'constraints' => [
                    new NotBlank(['message' => 'Name is required.']),
                    new Length([
                        'min' => 3, 'minMessage' => 'Minimum 3 characters.',
                        'max' => 200, 'maxMessage' => 'Maximum 200 characters.',
                    ]),
                ],
                'attr' => ['class' => 'form-control', 'placeholder' => "Event Name"],
            ])
            ->add('description', TextareaType::class, [
                'label' => 'Description',
                'required' => false,
                'attr' => ['class' => 'form-control', 'rows' => 4],
            ])
            ->add('startDate', DateType::class, [
                'label' => 'Start Date',
                'widget' => 'single_text',
                'constraints' => [
                    new NotBlank(['message' => 'Start date is required.']),
                ],
                'attr' => ['class' => 'form-control'],
            ])
            ->add('endDate', DateType::class, [
                'label' => 'End Date',
                'widget' => 'single_text',
                'constraints' => [
                    new NotBlank(['message' => 'End date is required.']),
                ],
                'attr' => ['class' => 'form-control'],
            ])
            ->add('status', ChoiceType::class, [
                'label' => 'Status',
                'choices' => [
                    'Scheduled' => 'SCHEDULED',
                    'Ongoing'   => 'ONGOING',
                    'Completed' => 'COMPLETED',
                    'Cancelled' => 'CANCELLED',
                ],
                'constraints' => [new NotBlank(['message' => 'Status is required.'])],
                'attr' => ['class' => 'form-control'],
            ])
            ->add('visibility', ChoiceType::class, [
                'label' => 'Visibility',
                'choices' => ['Public' => 'PUBLIC', 'Private' => 'PRIVATE'],
                'constraints' => [new NotBlank()],
                'attr' => ['class' => 'form-control'],
            ])
            ->add('capacity', IntegerType::class, [
                'label' => 'Capacity (max fighters)',
                'constraints' => [
                    new NotBlank(['message' => 'Capacity is required.']),
                    new Positive(['message' => 'Capacity must be positive.']),
                    new Range(['min' => 1, 'max' => 10000]),
                ],
                'attr' => ['class' => 'form-control'],
            ])
            ->add('venue', EntityType::class, [
                'class' => Venue::class,
                'choice_label' => function (Venue $venue) { return (string) $venue; },
                'label' => 'Venue',
                'placeholder' => '-- Select a venue --',
                'constraints' => [new NotNull(['message' => 'Venue is required.'])],
                'attr' => ['class' => 'form-control'],
            ])
            ->add('discipline', EntityType::class, [
                'class' => Discipline::class,
                'choice_label' => 'name',
                'label' => 'Sport Discipline',
                'placeholder' => '-- Select a discipline --',
                'constraints' => [new NotNull(['message' => 'Discipline is required.'])],
                'attr' => ['class' => 'form-control'],
            ]);

        // Custom callback: endDate >= startDate
        $builder->addEventListener(FormEvents::POST_SUBMIT, function (FormEvent $event) {
            $form = $event->getForm();
            $data = $event->getData();

            if ($data instanceof Event) {
                $start = $data->getStartDate();
                $end   = $data->getEndDate();
                if ($start && $end && $end < $start) {
                    $form->get('endDate')->addError(
                        new FormError('End date must be greater than or equal to start date.')
                    );
                }
            }
        });
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Event::class,
        ]);
    }
}
