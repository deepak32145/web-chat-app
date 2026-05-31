import React, { useState } from 'react';
import { FaSearch } from 'react-icons/fa';
import '../styles/UserSearchBar.css';

const UserSearchBar = ({ onSearch }) => {
  const [phoneNumber, setPhoneNumber] = useState('');

  const handleSearch = (e) => {
    e.preventDefault();
    if (phoneNumber.trim()) {
      onSearch(phoneNumber);
      setPhoneNumber('');
    }
  };

  return (
    <form className="search-bar" onSubmit={handleSearch}>
      <input
        type="tel"
        placeholder="Search by phone number"
        value={phoneNumber}
        onChange={(e) => setPhoneNumber(e.target.value)}
      />
      <button type="submit" className="search-btn">
        <FaSearch />
      </button>
    </form>
  );
};

export default UserSearchBar;
