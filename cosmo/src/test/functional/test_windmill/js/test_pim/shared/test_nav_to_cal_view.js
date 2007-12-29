
pimTest.shared.test_navToCalView = [
  { method: "waits.sleep", params: { milliseconds : 2000 } },
  { method: "click", params: {  id: "viewToggle_button1" } },
  { method: "waits.forElement", params: { id: "calViewNav" } }
];

