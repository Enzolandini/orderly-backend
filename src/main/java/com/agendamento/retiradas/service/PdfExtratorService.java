package com.agendamento.retiradas.service;

import com.agendamento.retiradas.dto.ItemPedidoDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PdfExtratorService {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DadosExtraidosPdf {
        private String nomeCliente;
        private String numeroPedido;
        private List<ItemPedidoDto> itens = new ArrayList<>();
    }

    public DadosExtraidosPdf extrairDadosDoPdf(String caminhoArquivo) throws IOException {
        File file = new File(caminhoArquivo);
        PDDocument document = PDDocument.load(file);
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(document);
        document.close();

        DadosExtraidosPdf dados = new DadosExtraidosPdf();
        
        // Extrair número do pedido
        Pattern patternNumeroPedido = Pattern.compile("Num\\. Orçamento : (\\d+)");
        Matcher matcherNumeroPedido = patternNumeroPedido.matcher(text);
        if (matcherNumeroPedido.find()) {
            dados.setNumeroPedido(matcherNumeroPedido.group(1));
        }
        
        // Extrair nome do cliente
        Pattern patternCliente = Pattern.compile("Cliente : (.+)");
        Matcher matcherCliente = patternCliente.matcher(text);
        if (matcherCliente.find()) {
            String clienteCompleto = matcherCliente.group(1);
            // Remover código numérico se existir (ex: "4 - FIXAGOLD COM. DE PARAFUSOS E ACES. LTDA")
            if (clienteCompleto.matches("\\d+ - .+")) {
                dados.setNomeCliente(clienteCompleto.replaceFirst("\\d+ - ", ""));
            } else {
                dados.setNomeCliente(clienteCompleto);
            }
        }
        
        // Extrair itens do pedido
        // Primeiro identificamos a seção de itens
        String[] linhas = text.split("\\r?\\n");
        boolean dentroSecaoItens = false;
        boolean cabecalhoEncontrado = false;
        
        for (String linha : linhas) {
            // Identificar o início da seção de itens pelo cabeçalho
            if (!cabecalhoEncontrado && linha.contains("Cod.") && linha.contains("Descricao") && linha.contains("Qt.(Un.)")) {
                cabecalhoEncontrado = true;
                dentroSecaoItens = true;
                continue;
            }
            
            // Identificar o fim da seção de itens
            if (dentroSecaoItens && linha.contains("Total :")) {
                dentroSecaoItens = false;
                continue;
            }
            
            // Processar linhas dentro da seção de itens
            if (dentroSecaoItens && !linha.trim().isEmpty()) {
                // Extrair dados do item usando regex ou split
                // Exemplo de padrão: "AGE1.1/2GF      ABRAÇ GOTA ECONOMICA 1.1/2 GF       73089010.   PC      150         6,000000        900,00"
                String[] partes = linha.trim().split("\\s{2,}"); // Dividir por 2 ou mais espaços
                
                if (partes.length >= 5) { // Verificar se temos pelo menos código, descrição, NCM, unidade e quantidade
                    ItemPedidoDto item = new ItemPedidoDto();
                    item.setCodigo(partes[0]);
                    item.setDescricao(partes[1]);
                    
                    // Extrair NCM, unidade e quantidade
                    if (partes.length >= 3) {
                        item.setNcm(partes[2]);
                    }
                    
                    if (partes.length >= 4) {
                        item.setUnidade(partes[3]);
                    }
                    
                    if (partes.length >= 5) {
                        try {
                            item.setQuantidade(Integer.parseInt(partes[4].trim()));
                        } catch (NumberFormatException e) {
                            item.setQuantidade(0);
                        }
                    }
                    
                    // Extrair preço unitário e valor total se disponíveis
                    if (partes.length >= 6) {
                        try {
                            String precoStr = partes[5].replace(",", ".");
                            item.setPrecoUnitario(Double.parseDouble(precoStr));
                        } catch (NumberFormatException e) {
                            item.setPrecoUnitario(0.0);
                        }
                    }
                    
                    if (partes.length >= 7) {
                        try {
                            String valorStr = partes[6].replace(",", ".");
                            item.setValorTotal(Double.parseDouble(valorStr));
                        } catch (NumberFormatException e) {
                            item.setValorTotal(0.0);
                        }
                    }
                    
                    dados.getItens().add(item);
                }
            }
        }
        
        return dados;
    }
}
