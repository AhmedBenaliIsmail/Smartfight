<?php

namespace App\Controller;

use App\Service\AIService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class ChatbotController extends AbstractController
{
    #[Route('/api/chat', name: 'app_api_chat', methods: ['POST'])]
    public function chat(Request $request, AIService $aiService): Response
    {
        $data = json_decode($request->getContent(), true);
        $message = $data['message'] ?? '';
        $history = $data['history'] ?? [];

        if (empty($message)) {
            return $this->json(['success' => false, 'reply' => 'Please provide a message.']);
        }

        $response = $aiService->chat($message, $history);

        return $this->json($response);
    }
}
