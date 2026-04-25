<?php

namespace App\Form;

use App\Entity\User;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\CheckboxType;
use Symfony\Component\Form\Extension\Core\Type\EmailType;
use Symfony\Component\Form\Extension\Core\Type\PasswordType;
use Symfony\Component\Form\Extension\Core\Type\RepeatedType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints\IsTrue;
use Symfony\Component\Validator\Constraints\Length;
use Symfony\Component\Validator\Constraints\NotBlank;

class RegistrationFormType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('firstName', TextType::class, [
                'label' => 'First name',
                'constraints' => [
                    new NotBlank(),
                    new Length(min: 2, max: 100),
                ],
            ])
            ->add('lastName', TextType::class, [
                'label' => 'Last name',
                'constraints' => [
                    new NotBlank(),
                    new Length(min: 2, max: 100),
                ],
            ])
            ->add('username', TextType::class, [
                'label' => 'Username',
                'constraints' => [
                    new NotBlank(),
                    new Length(min: 3, max: 50),
                ],
            ])
            ->add('email', EmailType::class, [
                'label' => 'Email',
                'constraints' => [
                    new NotBlank(),
                ],
            ])
            ->add('plainPassword', RepeatedType::class, [
                'type' => PasswordType::class,
                'mapped' => false,
                'first_options' => [
                    'label' => 'Password',
                    'constraints' => [
                        new NotBlank(),
                        new Length(min: 8, minMessage: 'Password must be at least {{ limit }} characters.'),
                    ],
                ],
                'second_options' => ['label' => 'Confirm password'],
                'invalid_message' => 'The password fields must match.',
            ])
            ->add('agreeTerms', CheckboxType::class, [
                'label' => 'I agree to the terms and conditions',
                'mapped' => false,
                'constraints' => [
                    new IsTrue(message: 'You must agree to the terms.'),
                ],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => User::class,
        ]);
    }
}
