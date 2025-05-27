package com.agendamento.retiradas.service;

import com.agendamento.retiradas.model.Pedido;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificacaoService {
    
    private final JavaMailSender emailSender;
    
    @Async
    public void notificarAprovacaoHorario(Pedido pedido) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(pedido.getVendedor().getEmail());
        message.setSubject("Horário de Retirada Aprovado - Pedido #" + pedido.getNumeroPedido());
        message.setText("Olá " + pedido.getVendedor().getName() + ",\n\n" +
                "O horário proposto para retirada do pedido #" + pedido.getNumeroPedido() + 
                " para o cliente " + pedido.getNomeCliente() + " foi APROVADO.\n\n" +
                "Horário confirmado: " + pedido.getHorarioAprovadoRetirada() + "\n\n" +
                "Atenciosamente,\n" +
                "Equipe de Expedição");
        
        emailSender.send(message);
    }
    
    @Async
    public void notificarReagendamentoHorario(Pedido pedido) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(pedido.getVendedor().getEmail());
        message.setSubject("Novo Horário Sugerido - Pedido #" + pedido.getNumeroPedido());
        message.setText("Olá " + pedido.getVendedor().getName() + ",\n\n" +
                "Um novo horário foi sugerido para a retirada do pedido #" + pedido.getNumeroPedido() + 
                " para o cliente " + pedido.getNomeCliente() + ".\n\n" +
                "Horário original proposto: " + pedido.getHorarioPropostoRetirada() + "\n" +
                "Novo horário sugerido: " + pedido.getHorarioAprovadoRetirada() + "\n\n" +
                "Atenciosamente,\n" +
                "Equipe de Expedição");
        
        emailSender.send(message);
    }
}
