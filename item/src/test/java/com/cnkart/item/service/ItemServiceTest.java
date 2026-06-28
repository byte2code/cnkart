package com.cnkart.item.service;

import com.cnkart.item.dto.ItemRequest;
import com.cnkart.item.dto.ItemResponse;
import com.cnkart.item.model.Item;
import com.cnkart.item.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateItem() {
        ItemRequest request = new ItemRequest("Mouse", "Wireless", new BigDecimal("499.00"));
        
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            item.setId(1L);
            return item;
        });

        itemService.createItem(request);

        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void testGetAllItems() {
        Item item1 = Item.builder().id(1L).name("Mouse").description("Wireless").price(new BigDecimal("499.00")).build();
        Item item2 = Item.builder().id(2L).name("Keyboard").description("Mechanical").price(new BigDecimal("999.00")).build();

        when(itemRepository.findAll()).thenReturn(Arrays.asList(item1, item2));

        List<ItemResponse> responses = itemService.getAllItems();

        assertEquals(2, responses.size());
        assertEquals("Mouse", responses.get(0).getName());
        assertEquals("Keyboard", responses.get(1).getName());
        verify(itemRepository, times(1)).findAll();
    }
}
