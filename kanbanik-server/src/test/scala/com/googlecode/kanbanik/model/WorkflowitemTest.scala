package com.googlecode.kanbanik.model
import org.bson.types.ObjectId

class WorkflowitemTest extends BaseIntegrationTest {
  describe("Workflowitem should be able to do all the CRUD operations") {

    it("should be able to store also nextItem") {
      val nextItem = Some(Workflowitem.byId(new ObjectId("4f48e10644ae3742baa2d0a9")))
      var stored = new Workflowitem(None, "name1", 1, "H", 1, None, nextItem, Board.byId(new ObjectId("1d48e10644ae3742baa2d0b9")))
      stored = stored.store
      val loaded = Workflowitem.byId(stored.id.getOrElse(notSet))
      val nextItemId = loaded.nextItem.getOrElse(notSet).id.get
      assert(nextItemId === "4f48e10644ae3742baa2d0a9")
    }
    
    it("should be able to find what it stored") {
      val stored = new Workflowitem(None, "name1", 1, "H", 1, None, None, board).store
      val loaded = Workflowitem.byId(stored.id.getOrElse(notSet))
      assert(stored.name === loaded.name)
      assert(stored.wipLimit === loaded.wipLimit)
      assert(stored.id === loaded.id)

    }

    it("should fail when incorrect id specified") {
      intercept[IllegalArgumentException] {
        Workflowitem.byId(new ObjectId("not existing"))
      }
    }

    it("should be able to store subworkflows") {
      val stored = new Workflowitem(None, "name1", 1, "H", 1, 
          Some(Workflowitem.byId(new ObjectId("4f48e10644ae3742baa2d0a9"))), None, board).store

      val child = Workflowitem.byId(stored.id.getOrElse(notSet)).child.getOrElse(notSet)
      assert(child.name === "some name")
    }

    it("should be able to update the primitive parts of the item") {
      val stored = new Workflowitem(Some(new ObjectId("4f48e10644ae3742baa2d0d9")), "other name", 2, "H", 1, None, None, board).store
      val loaded = Workflowitem.byId(new ObjectId("4f48e10644ae3742baa2d0d9"))
      assert(loaded.name === "other name")
      assert(loaded.wipLimit === 2)
    }

    it("should be possible to move the workflowitem from beginning to end") {
      val loaded = Workflowitem.byId(new ObjectId("1f48e10644ae3742baa2d0d9"))
      loaded.nextItem = None
      loaded.store
      assertItemsInOrder(List(
        Some(new ObjectId("2f48e10644ae3742baa2d0d9")),
        Some(new ObjectId("3f48e10644ae3742baa2d0d9")),
        Some(new ObjectId("1f48e10644ae3742baa2d0d9"))))
    }

    it("should be possible to move the workflowitem from end to beginning") {
      val loaded = Workflowitem.byId(new ObjectId("3f48e10644ae3742baa2d0d9"))
      loaded.nextItem = workflowitemOf("1f48e10644ae3742baa2d0d9")
      loaded.store
      assertItemsInOrder(List(
        Some(new ObjectId("3f48e10644ae3742baa2d0d9")),
        Some(new ObjectId("1f48e10644ae3742baa2d0d9")),
        Some(new ObjectId("2f48e10644ae3742baa2d0d9"))))
    }

    it("should be possible to move the workflowitem from middle to beginning") {
      val loaded = Workflowitem.byId(new ObjectId("2f48e10644ae3742baa2d0d9"))
      loaded.nextItem = workflowitemOf("1f48e10644ae3742baa2d0d9")
      loaded.store
      assertItemsInOrder(List(
        Some(new ObjectId("2f48e10644ae3742baa2d0d9")),
        Some(new ObjectId("1f48e10644ae3742baa2d0d9")),
        Some(new ObjectId("3f48e10644ae3742baa2d0d9"))))
    }

    it("should be possible to move the workflowitem from middle to end") {
      val loaded = Workflowitem.byId(new ObjectId("2f48e10644ae3742baa2d0d9"))
      loaded.nextItem = None
      loaded.store
      assertItemsInOrder(List(
        Some(new ObjectId("1f48e10644ae3742baa2d0d9")),
        Some(new ObjectId("3f48e10644ae3742baa2d0d9")),
        Some(new ObjectId("2f48e10644ae3742baa2d0d9"))))
    }

    it("should be possible to stay at the end") {
      val loaded = Workflowitem.byId(new ObjectId("3f48e10644ae3742baa2d0d9"))
      loaded.nextItem = None
      loaded.store
      assertItemsInOrder(List(
        Some(new ObjectId("1f48e10644ae3742baa2d0d9")),
        Some(new ObjectId("2f48e10644ae3742baa2d0d9")),
        Some(new ObjectId("3f48e10644ae3742baa2d0d9"))))
    }

    it("should be possible to stay where you are") {
      val loaded = Workflowitem.byId(new ObjectId("2f48e10644ae3742baa2d0d9"))
      loaded.nextItem = workflowitemOf("3f48e10644ae3742baa2d0d9");
      loaded.store
      assertItemsInOrder(List(
        Some(new ObjectId("1f48e10644ae3742baa2d0d9")),
        Some(new ObjectId("2f48e10644ae3742baa2d0d9")),
        Some(new ObjectId("3f48e10644ae3742baa2d0d9"))))
    }

    it("should be possible to move in more complex boards") {
      val loaded = Workflowitem.byId(new ObjectId("5a48e10644ae3742baa2d0d9"))
      loaded.nextItem = workflowitemOf("2a48e10644ae3742baa2d0d9")
      loaded.store
      assertItemsInOrder(List(
        Some(new ObjectId("1a48e10644ae3742baa2d0d9")),
        Some(new ObjectId("5a48e10644ae3742baa2d0d9")),
        Some(new ObjectId("2a48e10644ae3742baa2d0d9")),
        Some(new ObjectId("3a48e10644ae3742baa2d0d9")),
        Some(new ObjectId("4a48e10644ae3742baa2d0d9")),
        Some(new ObjectId("6a48e10644ae3742baa2d0d9"))))
    }

    it("should be possible to move in two elemnts board from beginning to end") {
      val loaded = Workflowitem.byId(new ObjectId("1b48e10644ae3742baa2d0d9"))
      loaded.nextItem = None
      loaded.store
      assertItemsInOrder(List(
        Some(new ObjectId("2b48e10644ae3742baa2d0d9")),
        Some(new ObjectId("1b48e10644ae3742baa2d0d9"))))
    }

    it("should be possible to move in two elemnts board from end to beginning") {
      val loaded = Workflowitem.byId(new ObjectId("2b48e10644ae3742baa2d0d9"))
      loaded.nextItem = workflowitemOf("1b48e10644ae3742baa2d0d9")
      loaded.store
      assertItemsInOrder(List(
        Some(new ObjectId("2b48e10644ae3742baa2d0d9")),
        Some(new ObjectId("1b48e10644ae3742baa2d0d9"))))
    }

    it("should be possible to delete the first element") {
      val loaded = Workflowitem.byId(new ObjectId("1f48e10644ae3742baa2d0d9"))
      loaded.delete
      assertItemsInOrder(List(
        Some(new ObjectId("2f48e10644ae3742baa2d0d9")),
        Some(new ObjectId("3f48e10644ae3742baa2d0d9"))))
    }

    it("should be possible to delete the last element") {
      val loaded = Workflowitem.byId(new ObjectId("3f48e10644ae3742baa2d0d9"))
      loaded.delete
      assertItemsInOrder(List(
        Some(new ObjectId("1f48e10644ae3742baa2d0d9")),
        Some(new ObjectId("2f48e10644ae3742baa2d0d9"))))
    }

    it("should be possible to delete the middle element") {
      val loaded = Workflowitem.byId(new ObjectId("2f48e10644ae3742baa2d0d9"))
      loaded.delete
      assertItemsInOrder(List(
        Some(new ObjectId("1f48e10644ae3742baa2d0d9")),
        Some(new ObjectId("3f48e10644ae3742baa2d0d9"))))
    }

    it("should be possible to delete all elements") {
      Workflowitem.byId(new ObjectId("1f48e10644ae3742baa2d0d9")).delete
      Workflowitem.byId(new ObjectId("2f48e10644ae3742baa2d0d9")).delete
      Workflowitem.byId(new ObjectId("3f48e10644ae3742baa2d0d9")).delete
    }

    it("should be possible add new children") {
      val loaded = Workflowitem.byId(new ObjectId("4f48e10644ae3742baa2d0a9"))
      val newChild = new Workflowitem(None, "added", 1, "H", 1, None, None, Board.byId(new ObjectId("1d48e10644ae3742baa2d0b9")))
      loaded.child = Some(newChild.store)
      loaded.store
      val toBeChanged = Workflowitem.byId(new ObjectId("4f48e10644ae3742baa2d0a9"))
      assert(loaded.child.get.name === "added")
    }
//
//    it("should remove reference from board to workflow when the workflow is deleted") {
//      Board.byId(new ObjectId("4f48e10644ae3742baa2d0d0")).workflowitems.get(0).delete
//      assert(Board.byId(new ObjectId("4f48e10644ae3742baa2d0d0")).workflowitems.get.size === 2)
//    }

    def workflowitemOf(id: String) = Some(Workflowitem.byId(new ObjectId(id)))

    def board = Board.byId(new ObjectId("1f48e10644ae3742baa2d0b9"))

    def assertItemsInOrder(ids: List[Option[ObjectId]]) {
      ids.foldLeft(ids.head) {
        (actual, expected) =>
          {
            assert(actual === expected)
            val next = Workflowitem.byId(expected.getOrElse(return )).nextItem
            if (!next.isDefined) {
              return ;
            } else {
              next.get.id
            }

          }
      }
    }

    def notSet = {
      throw new IllegalStateException("Required field is not set")
    }
  }
}
