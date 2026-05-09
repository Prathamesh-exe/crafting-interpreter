#include <stdio.h>
#include <stdlib.h>
#include <string.h>

// Node defination
typedef struct Node
{
    char *data;
    struct Node *next;
    struct Node *prev;
} Node;

// Doubly linked list defination
typedef struct DoublyLinkedList
{
    Node *head;
    Node *tail;
} DoublyLinkedList;

// create a new node
Node *createNode(const char *str)
{
    Node *node = (Node *)malloc(sizeof(Node));
    if (!node)
    {
        perror("malloc failed ");
        exit(1);
    }

    node->data = strdup(str);
    node->prev = node->next = NULL;
    return node;
}

// initialize list
void init_list(DoublyLinkedList *list)
{
    list->head = list->tail = NULL;
}

// insert at end
void insert(DoublyLinkedList *list, const char *str)
{
    Node *node = createNode(str);

    if (list->tail == NULL)
    {
        list->head = list->tail = node;
        return;
    }

    list->tail->next = node;
    node->prev = list->tail;
    list->tail = node;
}

// find node
Node *find(DoublyLinkedList *list, const char *str)
{
    Node *curr = list->head;

    while (curr)
    {
        if (strcmp(str, curr->data) == 0)
            return curr;
        curr = curr->next;
    }
    return NULL;
}

// delete node
void *delete(DoublyLinkedList *list, const char *str)
{
    Node *target = find(list, str);

    if (!target)
        return NULL;

    if (target->prev)
    {
        target->prev->next = target->next;
    }
    else
    {
        list->head = target->next;
    }

    if (target->next)
    {
        target->next->prev = target->prev;
    }
    else
    {
        list->tail = target->prev;
    }

    free(target->data);
    free(target);
}

// print list
void print_list(DoublyLinkedList *list)
{
    Node *curr = list->head;
    while (curr)
    {
        printf("%s <-> ", curr->data);
        curr = curr->next;
    }
    printf("NULL\n");
}

// free entire list
void free_list(DoublyLinkedList *list)
{
    Node *curr = list->head;
    while (curr)
    {
        Node *temp = curr;
        curr = curr->next;
        free(temp->data);
        free(temp);
    }
}

int main()
{
    DoublyLinkedList list;
    init_list(&list);

    insert(&list, "apple");
    insert(&list, "banana");
    insert(&list, "cherry");
    insert(&list, "date");

    print_list(&list);

    printf("Deleting 'banana'\n");
    delete(&list, "banana");
    print_list(&list);

    printf("Deleting 'apple'\n");
    delete(&list, "apple");
    print_list(&list);

    printf("Deleting 'date'\n");
    delete(&list, "date");
    print_list(&list);

    printf("Deleting 'not_present'\n");
    if (!delete(&list, "not_present"))
        printf("Item not found\n");

    /* Cleanup */
    free_list(&list);
}