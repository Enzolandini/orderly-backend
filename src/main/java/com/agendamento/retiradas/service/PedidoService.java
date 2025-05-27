package com.agendamento.retiradas.service;

import com.agendamento.retiradas.dto.PedidoDto;
import com.agendamento.retiradas.model.ItemPedido;
import com.agendamento.retiradas.model.Pedido;
import com.agendamento.retiradas.model.StatusPedido;
import com.agendamento.retiradas.model.User;
import com.agendamento.retiradas.repository.ItemPedidoRepository;
import com.agendamento.retiradas.repository.PedidoRepository;
import com.agendamento.retiradas.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final UserRepository userRepository;
    private final ItemPedidoRepository itemPedidoRepository;
    private final PdfExtratorService pdfExtratorService;
    private final NotificacaoService notificacaoService;
    
    private final Path fileStorageLocation = Paths.get("/uploads/pdfs").toAbsolutePath().normalize();
    
    public PedidoService() {
        this.pedidoRepository = null;
        this.userRepository = null;
        this.itemPedidoRepository = null;
        this.pdfExtratorService = null;
        this.notificacaoService = null;
        
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Não foi possível criar o diretório para armazenar os arquivos.", ex);
        }
    }

    @Transactional(readOnly = true)
    public List<Pedido> findAll() {
        return pedidoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Pedido> findById(Long id) {
        return pedidoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Pedido> findByVendedor(User vendedor) {
        return pedidoRepository.findByVendedor(vendedor);
    }

    @Transactional(readOnly = true)
    public List<Pedido> findByStatus(StatusPedido status) {
        return pedidoRepository.findByStatus(status);
    }

    @Transactional
    public Pedido processarNovoPedido(PedidoDto pedidoDto, MultipartFile pdfFile) throws IOException {
        // Salvar o arquivo PDF
        String fileName = UUID.randomUUID().toString() + "_" + pdfFile.getOriginalFilename();
        Path targetLocation = fileStorageLocation.resolve(fileName);
        Files.copy(pdfFile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        // Extrair dados do PDF
        var dadosExtraidos = pdfExtratorService.extrairDadosDoPdf(targetLocation.toString());
        
        // Buscar o vendedor
        User vendedor = userRepository.findById(pedidoDto.getVendedorId())
                .orElseThrow(() -> new RuntimeException("Vendedor não encontrado"));
        
        // Criar o pedido
        Pedido pedido = new Pedido();
        pedido.setNomeCliente(dadosExtraidos.getNomeCliente());
        pedido.setNumeroPedido(dadosExtraidos.getNumeroPedido());
        pedido.setDataRegistro(LocalDateTime.now());
        pedido.setHorarioPropostoRetirada(pedidoDto.getHorarioPropostoRetirada());
        pedido.setStatus(StatusPedido.PENDENTE);
        pedido.setObservacoes(pedidoDto.getObservacoes());
        pedido.setCaminhoArquivoPdf(targetLocation.toString());
        pedido.setVendedor(vendedor);
        
        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        
        // Adicionar os itens extraídos do PDF
        dadosExtraidos.getItens().forEach(item -> {
            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setCodigo(item.getCodigo());
            itemPedido.setDescricao(item.getDescricao());
            itemPedido.setQuantidade(item.getQuantidade());
            itemPedido.setUnidade(item.getUnidade());
            itemPedido.setNcm(item.getNcm());
            itemPedido.setPrecoUnitario(item.getPrecoUnitario());
            itemPedido.setValorTotal(item.getValorTotal());
            itemPedido.setPedido(pedidoSalvo);
            
            itemPedidoRepository.save(itemPedido);
        });
        
        return pedidoSalvo;
    }

    @Transactional
    public Pedido aprovarHorario(Long pedidoId, Long expedicaoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        
        User expedicao = userRepository.findById(expedicaoId)
                .orElseThrow(() -> new RuntimeException("Usuário da expedição não encontrado"));
        
        pedido.setStatus(StatusPedido.APROVADO);
        pedido.setHorarioAprovadoRetirada(pedido.getHorarioPropostoRetirada());
        pedido.setExpedicao(expedicao);
        
        Pedido pedidoAtualizado = pedidoRepository.save(pedido);
        
        // Enviar notificação ao vendedor
        notificacaoService.notificarAprovacaoHorario(pedidoAtualizado);
        
        return pedidoAtualizado;
    }

    @Transactional
    public Pedido sugerirNovoHorario(Long pedidoId, Long expedicaoId, LocalDateTime novoHorario) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));
        
        User expedicao = userRepository.findById(expedicaoId)
                .orElseThrow(() -> new RuntimeException("Usuário da expedição não encontrado"));
        
        pedido.setStatus(StatusPedido.REAGENDADO);
        pedido.setHorarioAprovadoRetirada(novoHorario);
        pedido.setExpedicao(expedicao);
        
        Pedido pedidoAtualizado = pedidoRepository.save(pedido);
        
        // Enviar notificação ao vendedor
        notificacaoService.notificarReagendamentoHorario(pedidoAtualizado);
        
        return pedidoAtualizado;
    }

    @Transactional
    public void delete(Long id) {
        pedidoRepository.deleteById(id);
    }
}
