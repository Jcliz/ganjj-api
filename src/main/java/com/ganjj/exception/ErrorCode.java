package com.ganjj.exception;

public enum ErrorCode {
    
    USER_EMAIL_ALREADY_EXISTS("ERR_USER_1001", "E-mail já cadastrado."),
    USER_PASSWORD_TOO_SHORT("ERR_USER_1002", "A senha deve ter no mínimo 6 caracteres."),
    USER_NOT_FOUND("ERR_USER_1003", "Usuário não encontrado com o ID: %s"),
    USER_EMAIL_ALREADY_EXISTS_FOR_ANOTHER("ERR_USER_1004", "E-mail já cadastrado para outro usuário."),
    USER_INVALID_ROLE("ERR_USER_1005", "Role inválida: %s"),
    USER_CANNOT_DELETE_LAST_ADMIN("ERR_USER_1006", "Não é possível excluir o último administrador do sistema."),
    USER_ACCESS_DENIED("ERR_USER_1007", "Você não tem permissão para acessar os dados de outro usuário."),
    
    PRODUCT_PRICE_INVALID("ERR_PRODUCT_2001", "O preço do produto deve ser maior que zero."),
    PRODUCT_STOCK_NEGATIVE("ERR_PRODUCT_2002", "A quantidade em estoque não pode ser negativa."),
    PRODUCT_NOT_FOUND("ERR_PRODUCT_2003", "Produto não encontrado com o ID: %s"),
    
    BRAND_NAME_ALREADY_EXISTS("ERR_BRAND_3001", "Já existe uma marca com este nome."),
    BRAND_NOT_FOUND("ERR_BRAND_3002", "Marca não encontrada com o ID: %s"),
    BRAND_HAS_PRODUCTS("ERR_BRAND_3003", "Não é possível excluir uma marca que possui produtos associados."),
    
    CATEGORY_NAME_ALREADY_EXISTS("ERR_CATEGORY_4001", "Já existe uma categoria com este nome."),
    CATEGORY_NOT_FOUND("ERR_CATEGORY_4002", "Categoria não encontrada com o ID: %s"),
    CATEGORY_HAS_PRODUCTS("ERR_CATEGORY_4003", "Não é possível excluir uma categoria que possui produtos."),
    
    ADDRESS_NOT_FOUND("ERR_ADDRESS_5001", "Endereço não encontrado com o ID: %s"),
    ADDRESS_ACCESS_DENIED("ERR_ADDRESS_5002", "Você não tem permissão para acessar este endereço."),
    ADDRESS_INVALID_CEP("ERR_ADDRESS_5003", "CEP inválido."),
    ADDRESS_CANNOT_EDIT_INACTIVE("ERR_ADDRESS_5004", "Não é possível editar um endereço inativo."),
    ADDRESS_CANNOT_DELETE_LAST_ACTIVE("ERR_ADDRESS_5005", "Não é possível excluir o único endereço ativo do usuário."),
    
    SHOPPING_BAG_NOT_FOUND("ERR_SHOPPING_BAG_6001", "Sacola de compras não encontrada com o ID: %s"),
    SHOPPING_BAG_NOT_OPEN("ERR_SHOPPING_BAG_6002", "Não é possível adicionar itens a uma sacola que não está aberta"),
    SHOPPING_BAG_INVALID_QUANTITY("ERR_SHOPPING_BAG_6003", "A quantidade deve ser maior que zero"),
    SHOPPING_BAG_CANNOT_MODIFY_CLOSED("ERR_SHOPPING_BAG_6004", "Não é possível alterar itens de uma sacola que não está aberta"),
    SHOPPING_BAG_ITEM_NOT_BELONGS("ERR_SHOPPING_BAG_6005", "O item não pertence à sacola especificada"),
    SHOPPING_BAG_CANNOT_REMOVE_FROM_CLOSED("ERR_SHOPPING_BAG_6006", "Não é possível remover itens de uma sacola que não está aberta"),
    SHOPPING_BAG_INVALID_STATUS("ERR_SHOPPING_BAG_6007", "Status inválido: %s"),
    SHOPPING_BAG_CANNOT_CLEAR_CLOSED("ERR_SHOPPING_BAG_6008", "Não é possível limpar uma sacola que não está aberta"),
    SHOPPING_BAG_ITEM_NOT_FOUND("ERR_SHOPPING_BAG_6009", "Item não encontrado na sacola com o ID: %s"),
    SHOPPING_BAG_ITEM_NOT_BELONGS_TO_BAG("ERR_SHOPPING_BAG_6010", "O item não pertence à sacola especificada"),
    
    ORDER_NOT_FOUND("ERR_ORDER_7001", "Pedido não encontrado com o ID: %s"),
    ORDER_INVALID_STATUS("ERR_ORDER_7002", "Status de pedido inválido: %s"),
    ORDER_CANNOT_BE_MODIFIED("ERR_ORDER_7003", "Este pedido não pode ser modificado."),
    ORDER_BAG_NOT_BELONGS_TO_USER("ERR_ORDER_7004", "A sacola não pertence ao usuário informado."),
    ORDER_BAG_IS_EMPTY("ERR_ORDER_7005", "A sacola está vazia. Não é possível criar um pedido."),
    ORDER_BAG_ALREADY_FINALIZED("ERR_ORDER_7006", "A sacola já foi finalizada."),
    ORDER_ADDRESS_NOT_BELONGS_TO_USER("ERR_ORDER_7007", "O endereço não pertence ao usuário informado."),
    ORDER_INSUFFICIENT_STOCK("ERR_ORDER_7008", "Estoque insuficiente para o produto: %s"),
    ORDER_CANNOT_DELETE_SENT_OR_DELIVERED("ERR_ORDER_7009", "Não é possível excluir um pedido que já foi enviado ou entregue."),
    
    REVIEW_RATING_INVALID("ERR_REVIEW_8001", "A avaliação deve estar entre 1 e 5 estrelas."),
    REVIEW_ALREADY_EXISTS("ERR_REVIEW_8002", "Você já avaliou este produto. Use a edição para atualizar sua avaliação."),
    REVIEW_NOT_FOUND("ERR_REVIEW_8003", "Avaliação não encontrada com o ID: %s"),
    REVIEW_ACCESS_DENIED("ERR_REVIEW_8004", "Você só pode editar ou deletar suas próprias avaliações."),
    REVIEW_ORDER_NOT_BELONGS_TO_USER("ERR_REVIEW_8005", "O pedido não pertence ao usuário informado."),
    
    AUTH_INVALID_CREDENTIALS("ERR_AUTH_9001", "Credenciais inválidas."),
    AUTH_TOKEN_EXPIRED("ERR_AUTH_9002", "Token expirado."),
    AUTH_TOKEN_INVALID("ERR_AUTH_9003", "Token inválido."),
    AUTH_ACCESS_DENIED("ERR_AUTH_9004", "Acesso negado."),
    AUTH_UNAUTHORIZED("ERR_AUTH_9005", "Não autorizado."),
    AUTH_REFRESH_TOKEN_INVALID("ERR_AUTH_9006", "Refresh token inválido."),
    
    GENERAL_INTERNAL_ERROR("ERR_GENERAL_0001", "Erro interno do servidor."),
    GENERAL_BAD_REQUEST("ERR_GENERAL_0002", "Requisição inválida."),
    GENERAL_VALIDATION_ERROR("ERR_GENERAL_0003", "Erro de validação."),
    GENERAL_METHOD_NOT_ALLOWED("ERR_GENERAL_0004", "Método HTTP não permitido."),
    GENERAL_UNSUPPORTED_MEDIA_TYPE("ERR_GENERAL_0005", "Tipo de mídia não suportado.");
    
    private final String code;
    private final String message;
    
    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String formatMessage(Object... args) {
        return String.format(message, args);
    }
}
